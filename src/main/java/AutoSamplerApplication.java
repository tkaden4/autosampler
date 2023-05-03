import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.sound.midi.MidiDevice;
import javax.sound.sampled.Mixer;
import jfxtras.styles.jmetro.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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

// TODO keep the visual state updated
public class AutoSamplerApplication extends Application {
  private Mixer.Info[] audioDevices;
  private MidiDevice.Info[] midiDevices;

  private ChoiceBox<Mixer.Info> audioDeviceChoice;
  private ChoiceBox<MidiDevice.Info> midiDeviceChoice;
  private File outputDirectory = Util.cwd().resolve("samples").toFile();
  private Text resultText;
  private Piano piano;
  private TextField sampleLengthField = new TextField("2000");
  private TextField noteHoldLengthField = new TextField("1000");
  private Button openDirectoryButton;
  private TextField directoryChooserTextField;

  private TextField startingNoteField = new TextField("C3");
  private TextField endingNoteField = new TextField("C6");
  private TextField intervalField = new TextField("6");

  private static String VERSION = "0.0.1";

  static void run(String[] args) {
    launch(args);
  }

  private AutoSampler.Options getOptionsFromState() {
    return new AutoSampler.Options(MIDIUtil.toMIDI(this.startingNoteField.getText()),
        MIDIUtil.toMIDI(this.endingNoteField.getText()),
        Integer.parseInt(this.intervalField.getText()),
        Integer.parseInt(this.sampleLengthField.getText()),
        Integer.parseInt(this.noteHoldLengthField.getText()), this.outputDirectory.toPath(),
        (note, velocity) -> "sample_" + note + "_" + velocity + ".wav", this.audioDeviceChoice.getValue(),
        this.midiDeviceChoice.getValue());
  }

  private void updateIOState() {
    this.openDirectoryButton.setDisable(!this.outputDirectory.exists());
    this.directoryChooserTextField.setText(this.outputDirectory.getPath().toString());
  }

  private void updatePianoState() {
    var options = this.getOptionsFromState();
    for (var code = options.startNote; code <= options.endNote; code += options.interval) {
      this.piano.highlight(code);
    }
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("Auto Sampler " + VERSION);
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
    this.directoryChooserTextField = new TextField(this.outputDirectory.toPath().toString());
    var directoryChooserButton = new Button("...");
    this.openDirectoryButton = new Button("View");
    this.openDirectoryButton.setOnAction(e -> {
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
      updateIOState();
    });
    updateIOState();

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

      this.outputDirectory.mkdirs();
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
          });
        } catch (Exception e) {
          Platform.runLater(() -> {
            self.resultText.setText(e.toString());
          });
        }
        Platform.runLater(() -> {
          sampleButton.setDisable(false);
          updateIOState();
        });
      });

      task.start();
    });

    // IO options
    var ioOptionsBox = new HBox(5, this.midiDeviceChoice, this.audioDeviceChoice, directoryChooserTextField,
        directoryChooserButton, openDirectoryButton);
    ioOptionsBox.getStyleClass().add(JMetroStyleClass.BACKGROUND);
    HBox.setHgrow(directoryChooserTextField, Priority.ALWAYS);

    // Timing options and start
    this.noteHoldLengthField = new TextField("1000");
    this.sampleLengthField = new TextField("2000");

    var sustainLabel = new Label("Note Sustain");
    var sampleLengthLabel = new Label("Sample Length");

    var startingNoteLabel = new Label("Start");
    var endingNoteLabel = new Label("End");

    var intervalLabel = new Label("Interval");

    this.startingNoteField.setMaxWidth(60);
    this.endingNoteField.setMaxWidth(60);
    this.intervalField.setMaxWidth(60);
    this.noteHoldLengthField.setMaxWidth(60);
    this.sampleLengthField.setMaxWidth(60);

    var controlsBox = new HBox(5, startingNoteLabel, this.startingNoteField, endingNoteLabel, this.endingNoteField,
        intervalLabel, this.intervalField,
        new Separator(Orientation.VERTICAL),
        sustainLabel, this.noteHoldLengthField, sampleLengthLabel, this.sampleLengthField,
        new Separator(Orientation.VERTICAL),
        sampleButton, estimateText);
    controlsBox.setAlignment(Pos.CENTER_LEFT);

    //
    var octaves = 7;
    var width = Piano.width(octaves);
    var pianoHeight = Piano.height();
    var height = 300 + pianoHeight;

    // Layout
    var utils = new VBox(10, ioOptionsBox, new Separator(Orientation.HORIZONTAL), controlsBox,
        new Separator(Orientation.HORIZONTAL));
    utils.setPadding(new Insets(10, 10, 10, 10));
    utils.maxHeight(300);

    // Piano
    this.piano = new Piano(width, pianoHeight, 7);
    this.piano.draw();
    var box = new VBox(this.piano.getCanvas(), new Separator(Orientation.HORIZONTAL), utils);
    box.getStyleClass().add(JMetroStyleClass.BACKGROUND);

    updatePianoState();

    // Scene
    var scene = new Scene(box, width, height);
    var theme = new JMetro(Style.DARK);
    theme.setScene(scene);

    primaryStage.setResizable(false);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}
