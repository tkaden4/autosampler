import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

public class Main {

    static AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

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
        var logger = Logger.getLogger("sampling");

        var sampler = new Sampler(new SamplerOptions(midiDeviceInfo, audioDeviceInfo, logger));

        var samples = new int[] { 60, 61, 62 };
        for (var sample : samples) {
            var file = Paths.get(System.getProperty("user.dir"), "samples", "sample_" + sample + ".wav");
            sampler.sample(sample, 127, 100, 2000, file);
            System.out.println("Finished recording " + file.toString());
        }
        System.out.println("Finished sampling");
    }
}
