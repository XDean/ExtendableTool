package xdean.tool.tray;

import java.awt.AWTException;
import java.io.IOException;

import xdean.tool.api.Context;

public class ToolMain {
  public static void main(String[] args) throws AWTException, IOException {
    TrayService.INSTANCE.start();
    TrayService.INSTANCE.load(Context.EXTENSION_PATH);
    // watchFile(Context.EXTENSION_PATH);
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
