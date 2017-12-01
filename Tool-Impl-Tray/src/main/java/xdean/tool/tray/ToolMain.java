package xdean.tool.tray;

import java.awt.AWTException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import xdean.tool.api.Context;

public class ToolMain {
  public static void main(String[] args) throws AWTException, IOException {
    TrayService.INSTANCE.start();
    loadTools(Context.EXTENSION_PATH);
//    watchFile(Context.EXTENSION_PATH);
  }

  private static void loadTools(Path dir) throws IOException {
    if (Files.exists(dir) && Files.isDirectory(dir)) {
      Files.newDirectoryStream(dir).forEach(path -> {
        if (!Files.isDirectory(path)) {
          TrayService.INSTANCE.addAll(ToolLoader.INSTANCE.getTools(path));
        }
      });
    }
  }

  /**
   * XXX Dean. Sometimes copy file cannot be listened
   *
   * @param dir
   */
  /* @Beta
  private static void watchFile(Path dir) {
    FileWatcherDslContext context = new FileWatcherDslContext();
    context.start(false)
        .watch(dir.toString())
        // .filter(FilterType.INCLUDE).extension(".jar")
        .on(FileType.FILE)
        .created(file -> ToolLoader.INSTANCE
            .getTools(Paths.get(file))
            .forEach(TrayService.INSTANCE::add));
    FileWatcherExecutor.getInstance().run(context).subscribe();
  }*/
}
