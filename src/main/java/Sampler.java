import java.io.IOException;
import java.nio.file.Path;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.midi.MidiDevice;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class Sampler {

  public static class Options {
    public MidiDevice.Info midiDevice;
    public Mixer.Info audioDevice;

    public Options(MidiDevice.Info midiDevice, Mixer.Info audioDevice) {
      this.midiDevice = midiDevice;
      this.audioDevice = audioDevice;
    }
  }

  private Options options;

  public Sampler(Options _options) {
    this.options = _options;
  }

  private static AudioFormat getAudioFormat() {
    return new AudioFormat(44100, 16, 1, true, false);
  }

  public void sample(int note, int velocity, int sustain, int duration, Path outputFilePath) throws Exception {
    var midiDevice = MidiSystem.getMidiDevice(this.options.midiDevice);
    var audioDevice = this.options.audioDevice;

    var outputFile = outputFilePath.toFile();
    var format = getAudioFormat();
    var line = AudioSystem.getTargetDataLine(format, audioDevice);

    line.open();
    line.start();
    midiDevice.open();

    if (!midiDevice.isOpen() || !line.isOpen()) {
      throw new IOException("Unable to open the necessary devices");
    }

    class Wrapper extends AudioInputStream {

      public Wrapper(TargetDataLine arg0) {
        super(arg0);
      }

      private boolean shouldStartChecking = false;
      private byte[] checkBuffer = new byte[4096];
      private int checkBufferPos = 0;
      private int checkBufferBytesRead = 0;

      public int read() throws java.io.IOException {
        return this.shouldStartChecking ? -1 : super.read();
      }

      public int read(byte[] arg0) throws java.io.IOException {
        var r = super.read(arg0);
        return r > 0 ? this.fillCheckBuffer(arg0, r) : r;
      }

      public int read(byte[] arg0, int arg1, int arg2) throws java.io.IOException {
        var r = super.read(arg0, arg1, arg2);
        return r > 0 ? this.fillCheckBuffer(arg0, r) : r;
      }

      private int fillCheckBuffer(byte[] data, int r) {
        if (!shouldStartChecking)
          return r;
        var endPosition = checkBufferPos;
        for (; endPosition < checkBuffer.length; ++endPosition) {
          if (endPosition - checkBufferPos < data.length) {
            this.checkBuffer[endPosition] = data[endPosition - checkBufferPos];
            ++this.checkBufferBytesRead;
          } else {
            break;
          }
        }

        this.checkBufferPos = endPosition % this.checkBuffer.length;

        var checkSum = 0;
        if (this.checkBufferBytesRead > this.checkBuffer.length) {
          for (var b : this.checkBuffer) {
            checkSum += Math.abs(b);
          }
          var utilization = (((float) checkSum) / (0.707 * 128 * this.checkBuffer.length));
          return utilization > 0.01 ? r : -1;
        }

        return r;
      }

      public synchronized void waitForFinish() {
        this.shouldStartChecking = true;
      }
    }

    var inputStream = new Wrapper(line);
    // We have to record on a separate thread
    var thread = new Thread(() -> {
      try {
        AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, outputFile);
      } catch (IOException e) {
        // TODO handle this better
        System.out.println(e);
      }
    });
    thread.start();

    var message = new ShortMessage();
    // Send the note that we wish to sample
    message.setMessage(ShortMessage.NOTE_ON, 0, note, velocity);
    var timeStamp = -1L;
    midiDevice.getReceiver().send(message, timeStamp);
    Thread.sleep(sustain);
    // Turn off the note that we have started sampling
    message.setMessage(ShortMessage.NOTE_OFF, 0, note, velocity);
    midiDevice.getReceiver().send(message, timeStamp);
    Thread.sleep(duration - sustain);
    // TODO wait for the sample to reach zero volume before returning, otherwise it
    // bleeds into the next sample

    midiDevice.close();

    System.out.println("Waiting for the sound to finish");
    inputStream.waitForFinish();

    thread.join();
    line.stop();
    line.close();
  }
}
