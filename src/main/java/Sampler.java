import java.io.IOException;
import java.nio.file.Path;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Sampler {
  private SamplerOptions options;

  public Sampler(SamplerOptions _options) {
    this.options = _options;
  }

  // TODO should this be choosable by the user?
  // and should it be automatically derived from the audio device?
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

    var inputStream = new AudioInputStream(line);

    // We have to record on a separate thread
    var thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, outputFile);
        } catch (IOException e) {
          System.out.println(e);
        }
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
    // Try to send the "all sound off" command
    // var offMessage = new SysexMessage(120, new byte[] {}, 0);
    // midiDevice.getReceiver().send(offMessage, timeStamp);

    midiDevice.close();
    line.stop();
    line.close();
    thread.interrupt();
    thread.join();
  }
}
