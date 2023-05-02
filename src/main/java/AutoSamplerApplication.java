import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.sound.sampled.Mixer;
import jfxtras.styles.jmetro.*;
import kotlin.Unit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
  private File outputDirectory = Util.cwd().resolve("samples").toFile();
  private Text resultText;
  private Piano piano;

  static void run(String[] args) {
    launch(args);
  }

  private AutoSampler.Options getOptionsFromState() {
    return new AutoSampler.Options(60, 71, 2000, 1000, this.outputDirectory.toPath(),
        (note, velocity) -> "sample_" + note + "_" + velocity + ".wav", this.audioDeviceChoice.getValue(),
        this.midiDeviceChoice.getValue());
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Auto Sampler v0");
    primaryStage.getIcons()
        .add(new Image(AutoSamplerApplication.class.getResourceAsStream("/icon128.png")));

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
    var directoryChooserTextField = new TextField(this.outputDirectory.toPath().toString());
    var directoryChooserButton = new Button("...");
    var openDirectoryButton = new Button("View");
    openDirectoryButton.setOnAction(e -> {
      try {
        Util.showPath(this.outputDirectory.toPath());
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });
    directoryChooserButton.setOnMouseClicked(e -> {
      var chosen = directoryChooser.showDialog(primaryStage);
      if (chosen == null) {
        return;
      }
      this.outputDirectory = chosen;
      directoryChooserTextField.setText(this.outputDirectory.getPath().toString());
    });

    // Start button + progress
    var outputText = new Text();
    this.resultText = outputText;
    var sampleButton = new Button("Start Sampling");
    var progress = new ProgressBar(0);
    var options = this.getOptionsFromState();
    var estimateText = new Text(
        Util.formatDuration(Duration.ofMillis(options.sampleLength * (options.endNote - options.startNote + 1))));

    sampleButton.setOnAction(_e -> {
      this.resultText.setText("");
      progress.setProgress(0);
      System.out.println(this.outputDirectory.toPath().toString());
      System.out.println("Starting sampling");

      sampleButton.setDisable(true);
      var self = this;
      var task = new Thread(() -> {
        try {
          AutoSampler.sample(getOptionsFromState(), (note, velocity, current, total) -> {
            final double currentProgress = ((double) current) / ((double) total);
            Platform.runLater(() -> {
              System.out.printf("%d %d %d/%d\n", note, velocity, current, total);
              progress.setProgress(currentProgress);
            });
            return Unit.INSTANCE;
          });
        } catch (Exception e) {
          Platform.runLater(() -> {
            self.resultText.setText(e.toString());
          });
        }
        Platform.runLater(() -> {
          sampleButton.setDisable(false);
        });
      });

      task.start();
    });

    // IO options
    var ioOptionsBox = new HBox(5, this.midiDeviceChoice, this.audioDeviceChoice, directoryChooserTextField,
        directoryChooserButton, openDirectoryButton);
    ioOptionsBox.getStyleClass().add(JMetroStyleClass.BACKGROUND);
    HBox.setHgrow(this.midiDeviceChoice, Priority.NEVER);
    HBox.setHgrow(this.audioDeviceChoice, Priority.NEVER);
    HBox.setHgrow(directoryChooserTextField, Priority.ALWAYS);
    HBox.setHgrow(directoryChooserButton, Priority.ALWAYS);
    HBox.setHgrow(openDirectoryButton, Priority.ALWAYS);

    //
    var octaves = 7;
    var width = Piano.width(octaves);
    var pianoHeight = Piano.height();
    var height = 300 + pianoHeight;

    // Layout
    var utils = new VBox(10, ioOptionsBox, new Separator(Orientation.HORIZONTAL),
        new HBox(5, sampleButton, estimateText), progress, outputText);
    utils.setPadding(new Insets(10, 10, 10, 10));
    utils.maxHeight(300);

    // Piano
    this.piano = new Piano(width, pianoHeight, 7);
    this.piano.draw();
    var box = new VBox(this.piano.getCanvas(), utils);
    box.getStyleClass().add(JMetroStyleClass.BACKGROUND);

    // Scene
    var scene = new Scene(box, width, height);
    var theme = new JMetro(Style.DARK);
    theme.setScene(scene);

    primaryStage.setResizable(false);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
