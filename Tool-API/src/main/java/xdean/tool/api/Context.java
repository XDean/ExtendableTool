package xdean.tool.api;

import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import xdean.jex.config.Config;

@UtilityClass
@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public class Context {
  String HOME_PATH = System.getProperty("user.home") + "/.xdean/tool";;
  Path HOME = Paths.get(HOME_PATH);
  Path EXTENSION_PATH = Paths.get(HOME_PATH + "/extension");
  Path TEMP_PATH = Paths.get(HOME_PATH + "/temp");
  Path DEFAULT_CONFIG_PATH = Paths.get("/default_config.properties");
  static {
    Config.locate(HOME.resolve("config.properties"), DEFAULT_CONFIG_PATH);
    uncheck(() -> Files.createDirectories(EXTENSION_PATH));
    uncheck(() -> Files.createDirectories(TEMP_PATH));
  }
}
