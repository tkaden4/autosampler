import java.nio.file.Path;

import kotlin.jvm.functions.Function2;

import javax.sound.midi.MidiDevice;
import javax.sound.sampled.Mixer;

public class AutoSampler {
  /**
   * TODO
   * Velocity layers
   * Chord sampling
   * Sample intervals
   */
  public static class Options {
    // First MIDI note to sample
    public int startNote;

    // Last MIDI note to sample
    public int endNote;

    // Length of time to sample (ms)
    // TODO replace with a generic function
    public int sampleLength;

    // Length of time to hold the note down (ms)
    public int noteLength;

    // Directory to place samples into
    public Path outputDirectory;

    // (note, velocity) -> name
    public Function2<Integer, Integer, String> namingConvention;

    // Audio Device
    public Mixer.Info audioDevice;

    // MIDI Device
    public MidiDevice.Info midiDevice;
  }

}
