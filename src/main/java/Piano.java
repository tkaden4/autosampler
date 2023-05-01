import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class Piano {
  static int WHITE_KEY_WIDTH = 20;
  static int WHITE_KEY_HEIGHT = 90;
  static int BLACK_KEY_WIDTH = (int) (WHITE_KEY_WIDTH / 1.71);
  static int BLACK_KEY_HEIGHT = (int) (WHITE_KEY_HEIGHT / 1.61);
  static int MARGIN = 2;
  static int PADDING = 5;
  static int NUDGE = 1;

  static int width(int octaves) {
    return PADDING + (octaves * 7) * (WHITE_KEY_WIDTH + MARGIN) + PADDING;
  }

  static Canvas create(int width, int height, int octaves) {
    var canvas = new Canvas(width, height);
    var ctx = canvas.getGraphicsContext2D();

    ctx.setFill(Color.valueOf("#1a1a1c"));
    ctx.fillRect(0, 0, width, height);

    var whiteKeys = octaves * 7;
    var blackKeys = octaves * 5;

    // draw the white keys
    ctx.setFill(Color.valueOf("#eaeaea"));
    for (int i = 0; i < whiteKeys; ++i) {
      var x = PADDING + i * (WHITE_KEY_WIDTH + MARGIN);
      var y = PADDING;
      ctx.fillRect(x, y, WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT);
    }

    // draw the black keys
    ctx.setFill(Color.valueOf("#0a0a0c"));

    var blackKeyOffsets = new int[] { 0, 1, 3, 4, 5 };
    var blackKeyNudge = new int[] { -NUDGE, NUDGE, -NUDGE, 0, NUDGE };
    for (int i = 0; i < octaves; ++i) {
      for (int offset = 0; offset < blackKeyOffsets.length; ++offset) {
        var x = PADDING + (i * (WHITE_KEY_WIDTH + MARGIN) * 7) + (blackKeyOffsets[offset] * (WHITE_KEY_WIDTH + MARGIN))
            + (WHITE_KEY_WIDTH + MARGIN) - ((BLACK_KEY_WIDTH) / 2.0) + blackKeyNudge[offset] - MARGIN / 2.0;
        var y = PADDING;
        ctx.fillRect(x, y, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT);
      }
    }

    return canvas;
  }
}
