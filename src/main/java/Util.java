import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class Util {
  static Path cwd() {
    return Paths.get(System.getProperty("user.dir"));
  }

  public static String formatDuration(Duration duration) {
    long seconds = duration.getSeconds();
    long absSeconds = Math.abs(seconds);
    String positive = String.format(
        "%d:%02d:%02d",
        absSeconds / 3600,
        (absSeconds % 3600) / 60,
        absSeconds % 60);
    return seconds < 0 ? "-" + positive : positive;
  }
}
