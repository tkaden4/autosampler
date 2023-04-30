import java.util.Arrays;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Mixer.Info;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AutoSamplerApplication extends Application {
  private Mixer.Info[] audioDevices;
  private MidiDevice.Info[] midiDevices;

  private ChoiceBox<Mixer.Info> audioDeviceChoice;
  private ChoiceBox<MidiDevice.Info> midiDeviceChoice;

  static void run(String[] args) {
    launch(args);
  }

  private AutoSampler.Options getOptionsFromState() {
    return null;
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("AutoSampler");

    this.audioDevices = Stream.of(AudioSystem.getMixerInfo()).filter(x -> SoundUtil.hasTargetDataLines(x))
        .toArray(s -> new Mixer.Info[s]);
    this.midiDevices = Stream.of(MidiSystem.getMidiDeviceInfo()).filter(x -> {
      return SoundUtil.hasMIDIOutput(x);
    })
        .toArray(s -> new MidiDevice.Info[s]);

    Arrays.sort(this.audioDevices, (a, b) -> a.getName().compareTo(b.getName()));
    Arrays.sort(this.midiDevices, (a, b) -> a.getName().compareTo(b.getName()));

    // Create the midi device choice box
    this.midiDeviceChoice = new ChoiceBox<>();
    this.midiDeviceChoice.setValue(this.midiDevices[0]);
    Stream.of(this.midiDevices).forEach(device -> {
      this.midiDeviceChoice.getItems().add(device);
    });

    // Create the audio device choicebox
    this.audioDeviceChoice = new ChoiceBox<>();
    this.audioDeviceChoice.setValue(this.audioDevices[0]);
    Stream.of(this.audioDevices).forEach(device -> {
      this.audioDeviceChoice.getItems().add(device);
    });

    var box = new HBox(10, this.midiDeviceChoice, this.audioDeviceChoice);

    var scene = new Scene(box, 700, 400);

    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
