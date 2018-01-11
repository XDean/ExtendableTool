package xdean.tool.api;

import static xdean.jex.util.lang.ExceptionUtil.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Context {
  public static final String HOME_PATH = System.getProperty("user.home") + "/.xdean/tool";;
  public static final Path HOME = Paths.get(HOME_PATH);
  public static final Path EXTENSION_PATH = Paths.get(HOME_PATH + "/extension");
  public static final Path TEMP_PATH = Paths.get(HOME_PATH + "/temp");
  public static final Path DEFAULT_CONFIG_PATH = uncheck(() -> getPath(Context.class.getResource("/default_config.properties").toURI()));
  static {
    Config.locate(HOME.resolve("config.properties"), DEFAULT_CONFIG_PATH);
    uncheck(() -> Files.createDirectories(EXTENSION_PATH));
    uncheck(() -> Files.createDirectories(TEMP_PATH));
  }

  public static Path getPath(URI uri) {
    try {
      return Paths.get(uri);
    } catch (FileSystemNotFoundException e) {
      Map<String, String> env = new HashMap<>();
      env.put("create", "true");
      try {
        FileSystems.newFileSystem(uri, env);
        return Paths.get(uri);
      } catch (IOException ee) {
        ee.addSuppressed(e);
        throw new Error(ee);
      }
    }
  }
}
