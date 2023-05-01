import java.nio.file.Path;
import java.nio.file.Paths;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;

import javax.sound.midi.MidiDevice;
import javax.sound.sampled.Mixer;
import java.util.Objects;

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
    public int sustainLength;

    // Directory to place samples into
    public Path outputDirectory;

    // (note, velocity) -> name
    public Function2<Integer, Integer, String> namingConvention;

    // Audio Device
    public Mixer.Info audioDevice;

    // MIDI Device
    public MidiDevice.Info midiDevice;

    public Options(int startNote, int endNote, int sampleLength, int sustainLength, Path outputDirectory,
        Function2<Integer, Integer, String> namingConvention, Mixer.Info audioDevice, MidiDevice.Info midiDevice) {
      this.startNote = startNote;
      this.endNote = endNote;
      this.sampleLength = sampleLength;
      this.sustainLength = sustainLength;
      this.outputDirectory = outputDirectory;
      this.namingConvention = namingConvention;
      this.audioDevice = audioDevice;
      this.midiDevice = midiDevice;
    }
  }

  // options, (note, velocity, current, total) -> void
  static void sample(Options options, Function4<Integer, Integer, Integer, Integer, Unit> onSample) throws Exception {
    var sampler = new Sampler(new Sampler.Options(options.midiDevice, options.audioDevice));
    for (int i = options.startNote; i <= options.endNote; ++i) {
      var path = Paths.get(options.outputDirectory.toString(), options.namingConvention.invoke(i, 127));
      sampler.sample(i, 127, options.sustainLength, options.sampleLength, path);
      onSample.invoke(i, 127, 1 + i - options.startNote, 1 + (options.endNote - options.startNote));
    }
  }
}
