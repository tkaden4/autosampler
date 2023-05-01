import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {
  static Path cwd() {
    return Paths.get(System.getProperty("user.dir"));
  }
}
