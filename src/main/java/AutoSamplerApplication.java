import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import jfxtras.styles.jmetro.*;
import kotlin.Unit;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

// TODO add time estimation
public class AutoSamplerApplication extends Application {
  private Mixer.Info[] audioDevices;
  private MidiDevice.Info[] midiDevices;

  private ChoiceBox<Mixer.Info> audioDeviceChoice;
  private ChoiceBox<MidiDevice.Info> midiDeviceChoice;
  private Text outputDirectoryText;
  private File outputDirectory = Util.cwd().toFile();
  private Text resultText;

  static void run(String[] args) {
    launch(args);
  }

  private AutoSampler.Options getOptionsFromState() {
    return new AutoSampler.Options(60, 72, 1000, 100, this.outputDirectory.toPath(),
        (note, velocity) -> "sample_" + note + "_" + velocity + ".wav", this.audioDeviceChoice.getValue(),
        this.midiDeviceChoice.getValue());
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Auto Sampler v0");

    this.audioDevices = SoundUtil.audioInputs();
    this.midiDevices = SoundUtil.midiOutputs();

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

    // Choice of output directory
    var directoryChooser = new DirectoryChooser();
    directoryChooser.setInitialDirectory(Util.cwd().toFile());
    var directoryChooserOpenButton = new Button("Choose Output");
    directoryChooserOpenButton.setOnAction(_e -> {
      this.outputDirectory = directoryChooser.showDialog(primaryStage);
      this.outputDirectoryText.setText(this.outputDirectory.getPath().toString());
    });
    this.outputDirectoryText = new Text(this.outputDirectory.toPath().toString());

    // Start button + progress
    var outputText = new Text();
    this.resultText = outputText;
    var sampleButton = new Button("Start Sampling");
    var progress = new ProgressBar(0);
    // TODO this needs to be run on a separate thread
    sampleButton.setOnAction(_e -> {
      this.resultText.setText("");
      progress.setProgress(0);
      System.out.println(this.outputDirectory.toPath().toString());
      System.out.println("Starting sampling");
      try {
        AutoSampler.sample(getOptionsFromState(), (note, velocity, current, total) -> {
          System.out.printf("%d %d %d/%d\n", note, velocity, current, total);
          progress.setProgress(((float) current) / ((float) total));
          return Unit.INSTANCE;
        });
      } catch (Exception e) {
        this.resultText.setText(e.toString());
      }
    });

    // IO options
    var ioOptionsBox = new HBox(5, this.midiDeviceChoice, this.audioDeviceChoice);
    var directoryChoiceBox = new HBox(5, directoryChooserOpenButton, this.outputDirectoryText);

    var box = new VBox(10, ioOptionsBox, new Separator(Orientation.HORIZONTAL), directoryChoiceBox,
        new Separator(Orientation.HORIZONTAL), sampleButton, progress, outputText);
    box.getStyleClass().add(JMetroStyleClass.BACKGROUND);
    box.setPadding(new Insets(10, 10, 10, 10));

    var scene = new Scene(box, 800, 600);

    var theme = new JMetro(Style.DARK);
    theme.setScene(scene);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
