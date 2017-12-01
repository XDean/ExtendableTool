package xdean.tool.api;

import static xdean.jex.util.lang.ExceptionUtil.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Context {
  public static final String HOME_PATH = System.getProperty("user.home") + "/.xdean/tool";;
  public static final Path HOME = Paths.get(HOME_PATH);
  public static final Path EXTENSION_PATH = Paths.get(HOME_PATH + "/extension");
  public static final Path TEMP_PATH = Paths.get(HOME_PATH + "/temp");
  public static final Path DEFAULT_CONFIG_PATH = uncatch(() -> Paths.get(Context.class.getResource("/default_config.properties").toURI()));
  static {
    Config.locate(HOME.resolve("config.properties"), DEFAULT_CONFIG_PATH);
    uncheck(() -> Files.createDirectories(EXTENSION_PATH));
    uncheck(() -> Files.createDirectories(TEMP_PATH));
  }
}
