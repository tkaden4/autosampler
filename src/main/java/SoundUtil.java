import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;

public class SoundUtil {

  static boolean hasTargetDataLines(Mixer.Info device) {
    var mixer = AudioSystem.getMixer(device);
    var targetLines = mixer.getTargetLineInfo();
    for (var line : targetLines) {
      if (line instanceof DataLine.Info) {
        return true;
      }
    }
    return false;
  }

  static boolean hasMIDIOutput(MidiDevice.Info midiDevice) {
    try {
      var info = MidiSystem.getMidiDevice(midiDevice);
      if (info.getMaxReceivers() == 0) {
        return false;
      }
      // The MidiOutDevice class isnt publicly exported, and i couldnt
      // find another way to see if a device was a real device and not
      // just a virtual device like the Java synthesizer
      var deviceClass = info.getClass();
      return deviceClass.getSimpleName().contains("MidiOutDevice");
    } catch (MidiUnavailableException e) {
      // TODO log or handle other way
      return false;
    }
  }
}
