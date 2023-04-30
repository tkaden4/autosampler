import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AutoSamplerApplication extends Application {
  private Mixer.Info[] audioDevices;
  private MidiDevice.Info[] midiDevices;

  static void run(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("AutoSampler");

    this.audioDevices = AudioSystem.getMixerInfo();
    this.midiDevices = MidiSystem.getMidiDeviceInfo();

    var midiChoice = new ChoiceBox<>();
    Stream.of(this.midiDevices).forEach(device -> {
      midiChoice.getItems().add(device.getName());
    });

    var audioChoice = new ChoiceBox<>();
    Stream.of(this.audioDevices).forEach(device -> {
      audioChoice.getItems().add(device.getName());
    });

    var box = new HBox(10, midiChoice, audioChoice);

    var scene = new Scene(box, 700, 400);

    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
