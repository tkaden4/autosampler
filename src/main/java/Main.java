import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Main {

    static AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    static File outputFile = Paths.get(System.getProperty("user.dir"), "sample.wav").toFile();

    static AudioFormat getAudioFormat() {
        return new AudioFormat(44100, 16, 1, true, false);
    }

    public static void main(String[] args) throws Exception {
        // Prepare the midi device
        var midiDevices = MidiSystem.getMidiDeviceInfo();
        var midiDeviceInfo = Arrays.stream(midiDevices).filter(x -> x.getName().contains("SOLO MIDI Out")).findFirst()
                .get();
        var midiDevice = MidiSystem.getMidiDevice(midiDeviceInfo);
        midiDevice.open();

        // Open the audio device
        var audioDevices = AudioSystem.getMixerInfo();
        var audioDeviceInfo = Arrays.stream(audioDevices).filter(x -> x.getName().startsWith("Line In 1/2")).findFirst()
                .get();

        // Print out the device info
        System.out.println("MIDI: " + midiDeviceInfo.toString());
        System.out.println("Audio: " + audioDeviceInfo.toString());

        var format = getAudioFormat();
        var line = AudioSystem.getTargetDataLine(format, audioDeviceInfo);
        line.open();
        line.start();
        var inputStream = new AudioInputStream(line);

        var thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AudioSystem.write(inputStream, fileType, outputFile);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

        });

        thread.start();

        // 1s of C4, max velocity
        var message = new ShortMessage();
        message.setMessage(ShortMessage.NOTE_ON, 0, 60, 127);
        var timeStamp = -1L;
        midiDevice.getReceiver().send(message, timeStamp);
        Thread.sleep(500);
        message.setMessage(ShortMessage.NOTE_OFF, 0, 60, 127);
        midiDevice.getReceiver().send(message, timeStamp);
        Thread.sleep(2000);

        midiDevice.close();
        line.stop();
        line.close();
        thread.join();
        System.out.println("Finished recording " + outputFile.getPath());
    }
}
