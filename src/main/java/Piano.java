import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Piano {
  private static class Key {
    public static enum Type {
      BLACK,
      WHITE
    }

    public boolean pressed;
    public Type type;
    public int number;
    public double x;
    public double y;

    public Key(Type _type, int _number, boolean _pressed, double _x, double _y) {
      this.pressed = _pressed;
      this.number = _number;
      this.type = _type;
      this.x = _x;
      this.y = _y;
    }
  }

  private static double WHITE_KEY_WIDTH = 20;
  private static double WHITE_KEY_HEIGHT = WHITE_KEY_WIDTH * 4.17;
  private static double BLACK_KEY_WIDTH = (double) (WHITE_KEY_WIDTH / 1.61);
  private static double BLACK_KEY_HEIGHT = (double) (WHITE_KEY_HEIGHT / 1.61);
  private static double MARGIN = 2;
  private static double PADDING = 8;
  private static double NUDGE = 2;

  private static String WHITE_KEY_COLOR = "#eaeaec";
  private static String BLACK_KEY_COLOR = "#0a0a0c";

  private static String WHITE_KEY_PRESSED_COLOR = "#facacc";
  private static String BLACK_KEY_PRESSED_COLOR = "#5a0a0c";

  private static String PIANO_BACKGROUND = "#1a1a1c";

  static double width(int octaves) {
    return PADDING + (octaves * 7) * (WHITE_KEY_WIDTH + MARGIN) + PADDING - MARGIN;
  }

  static double height() {
    return PADDING + WHITE_KEY_HEIGHT + PADDING;
  }

  private static double whiteKeyX(int key) {
    return PADDING + key * (WHITE_KEY_WIDTH + MARGIN);
  }

  private static double whiteKeyY(int key) {
    return PADDING;
  }

  private static int[] BLACK_KEY_OFFSETS = new int[] { 1, 3, 6, 8, 10 };
  private static int[] WHITE_KEY_OFFSETS = new int[] { 0, 2, 4, 5, 7, 9, 11 };
  private static double[] BLACK_KEY_NUDGE = new double[] { -NUDGE, NUDGE, -NUDGE, 0, NUDGE };

  private static double blackKeyX(int octave, int offset) {
    var OFFSETS_OF_WHITE = new int[] { 0, 1, 3, 4, 5 };
    return PADDING + (octave * (WHITE_KEY_WIDTH + MARGIN) * 7)
        + ((OFFSETS_OF_WHITE[offset]) * (WHITE_KEY_WIDTH + MARGIN))
        + (WHITE_KEY_WIDTH + MARGIN) - ((BLACK_KEY_WIDTH) / 2.0) + BLACK_KEY_NUDGE[offset] - MARGIN / 2.0;
  }

  private static double blackKeyY(int octave, int offset) {
    return PADDING;
  }

  private static boolean inBlackKey(int octave, int offset, int x, int y) {
    var xPosition = blackKeyX(octave, offset);
    var xPositionEnd = xPosition + BLACK_KEY_WIDTH;
    var yPosition = blackKeyY(octave, offset);
    var yPositionEnd = yPosition + BLACK_KEY_HEIGHT;

    return (x >= xPosition && x <= xPositionEnd) && (y >= yPosition && y <= yPositionEnd);
  }

  private static boolean inWhiteKey(int key, int x, int y) {
    var xPosition = whiteKeyX(key);
    var xPositionEnd = xPosition + WHITE_KEY_WIDTH;
    var yPosition = whiteKeyY(key);
    var yPositionEnd = yPosition + WHITE_KEY_HEIGHT;

    return (x >= xPosition && x <= xPositionEnd) && (y >= yPosition && y <= yPositionEnd);
  }

  private Key[] keys;
  private int octaves;
  private Canvas canvas;
  private double width;
  private double height;

  public Canvas getCanvas() {
    return this.canvas;
  }

  public void draw() {
    var ctx = this.canvas.getGraphicsContext2D();
    ctx.setFill(Color.valueOf(PIANO_BACKGROUND));
    ctx.fillRect(0, 0, width, height);

    // We need to render the white keys first
    for (int i = 0; i < this.keys.length; ++i) {
      var key = this.keys[i];
      if (key.type == Key.Type.WHITE) {
        ctx.setFill(key.pressed ? Color.valueOf(WHITE_KEY_PRESSED_COLOR) : Color.valueOf(WHITE_KEY_COLOR));
        ctx.fillRect(key.x, key.y, WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT);
      }
    }

    // Render the black keys next
    for (int i = 0; i < this.keys.length; ++i) {
      var key = this.keys[i];
      if (key.type == Key.Type.BLACK) {
        ctx.setFill(key.pressed ? Color.valueOf(BLACK_KEY_PRESSED_COLOR) : Color.valueOf(BLACK_KEY_COLOR));
        ctx.fillRect(key.x, key.y, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT);
      }
    }
  }

  public Piano(double _width, double _height, int _octaves) {
    this.octaves = _octaves;
    this.keys = new Key[_octaves * 12];
    this.width = _width;
    this.height = _height;
    this.canvas = new Canvas(_width, _height);
    for (int i = 0; i < _octaves; ++i) {
      for (int j = 0; j < 5; ++j) {
        var number = i * 12 + BLACK_KEY_OFFSETS[j];
        this.keys[number] = new Key(Key.Type.BLACK, number, false, blackKeyX(i, j), blackKeyY(i, j));
      }
      for (int j = 0; j < 7; ++j) {
        var number = i * 12 + WHITE_KEY_OFFSETS[j];
        this.keys[number] = new Key(Key.Type.WHITE, number, false, whiteKeyX(i * 7 + j),
            whiteKeyY(i * 7 + j));
      }
    }
  }
}
