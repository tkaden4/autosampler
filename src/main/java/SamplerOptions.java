import java.util.logging.Logger;

import javax.sound.midi.MidiDevice;
import javax.sound.sampled.Mixer;

public class SamplerOptions {
  public MidiDevice.Info midiDevice;
  public Mixer.Info audioDevice;
  public Logger logger;

  public SamplerOptions(MidiDevice.Info midiDevice, Mixer.Info audioDevice, Logger logger) {
    this.midiDevice = midiDevice;
    this.audioDevice = audioDevice;
    this.logger = logger;
  }
}
