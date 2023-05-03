import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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

    // Interval to sample between the notes
    public int interval;

    // Length of time to sample (ms)
    public int sampleLength;

    // Length of time to hold the note down (ms)
    public int noteHoldDuration;

    // Directory to place samples into
    public Path outputDirectory;

    // (note, velocity) -> name
    public NamingConvention namingConvention;

    // Audio Device
    public Mixer.Info audioDevice;

    // MIDI Device
    public MidiDevice.Info midiDevice;

    public static interface NamingConvention {
      String invoke(int note, int velocity);
    }

    public Options(int startNote, int endNote, int interval, int sampleLength, int noteHoldDuration,
        Path outputDirectory,
        NamingConvention namingConvention, Mixer.Info audioDevice, MidiDevice.Info midiDevice) {
      this.startNote = startNote;
      this.endNote = endNote;
      this.interval = interval;
      this.sampleLength = sampleLength;
      this.noteHoldDuration = noteHoldDuration;
      this.outputDirectory = outputDirectory;
      this.namingConvention = namingConvention;
      this.audioDevice = audioDevice;
      this.midiDevice = midiDevice;
    }
  }

  public static interface SampleHandler {
    void invoke(int note, int velocity, int current, int total);
  }

  // options, (note, velocity, current, total) -> void
  static void sample(Options options, SampleHandler onSample) throws Exception {
    var sampler = new Sampler(new Sampler.Options(options.midiDevice, options.audioDevice));
    var toSample = new HashSet<Integer>();
    for (var i = options.startNote; i <= options.endNote; i += options.interval) {
      toSample.add(i);
    }
    toSample.add(options.endNote);
    for (var note : toSample) {
      var path = Paths.get(options.outputDirectory.toString(), options.namingConvention.invoke(note, 127));
      sampler.sample(note, 127, options.noteHoldDuration, options.sampleLength, path);
      var current = 1 + note - options.startNote;
      var total = 1 + (options.endNote - options.startNote);
      onSample.invoke(note, 127, current, total);
    }
  }
}
