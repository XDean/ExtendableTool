package xdean.tool.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import xdean.jex.config.Config;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Context {
  public static final String HOME_PATH = System.getProperty("user.home") + "/.xdean/tool";;
  public static final Path HOME = Paths.get(HOME_PATH);
  public static final Path EXTENSION_PATH = Paths.get(HOME_PATH + "/extension");
  public static final Path TEMP_PATH = Paths.get(HOME_PATH + "/temp");
  public static final Path DEFAULT_CONFIG_PATH = Paths.get("/default_config.properties");
  static {
    Config.locate(HOME.resolve("config.properties"), DEFAULT_CONFIG_PATH);
    createFolderIfAbsent(EXTENSION_PATH);
    createFolderIfAbsent(TEMP_PATH);
  }

  private static void createFolderIfAbsent(Path path) {
    if (Files.notExists(path)) {
      try {
        Files.createDirectories(path);
      } catch (IOException e) {
        e.printStackTrace();
        log.error("Can't create folder", path, e);
      }
    }
  }
}
