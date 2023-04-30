import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioSystem;

public class AutoSamplerCLI {
  static <T> T showMenu(String title, T[] options, Function<T, String> formatter) {
    var scanner = new Scanner(System.in);
    Integer choice = null;
    while (choice == null) {
      System.out.print("\033[H\033[2J");
      System.out.flush();
      System.out.println("[ " + title + " ]");
      for (int i = 0; i < options.length; ++i) {
        System.out.printf("%d) %s\n", i, formatter.apply(options[i]));
      }
      System.out.print("choose device: ");
      choice = Integer.parseInt(scanner.nextLine().trim());
    }
    scanner.close();
    return options[choice];
  }

  public static void run(String[] args) throws Exception {
    var midiDevices = MidiSystem.getMidiDeviceInfo();
    var audioDevices = AudioSystem.getMixerInfo();

    var midiDeviceInfo = showMenu("MIDI Devices", midiDevices, x -> x.getName());
    var audioDeviceInfo = showMenu("Audio Devices", audioDevices, x -> x.getName());

    // Print out the device info
    System.out.println("MIDI: " + midiDeviceInfo.toString());
    System.out.println("Audio: " + audioDeviceInfo.toString());
    var logger = Logger.getLogger("sampling");

    var sampler = new Sampler(new Sampler.Options(midiDeviceInfo, audioDeviceInfo, logger));

    var samples = new int[] { 60, 61, 62 };
    for (var sample : samples) {
      var file = Paths.get(System.getProperty("user.dir"), "samples", "sample_" + sample + ".wav");
      sampler.sample(sample, 127, 100, 2000, file);
      System.out.println("Finished recording " + file.toString());
    }
    System.out.println("Finished sampling");
  }
}
