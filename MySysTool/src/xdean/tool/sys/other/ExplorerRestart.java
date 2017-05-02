package xdean.tool.sys.other;

import lombok.SneakyThrows;
import xdean.tool.api.impl.AbstractToolMenu;

public class ExplorerRestart extends AbstractToolMenu {
  public ExplorerRestart() {
    super("RestartExplorer");
  }

  @SneakyThrows
  @Override
  public void onClick() {
    Process close = Runtime.getRuntime().exec("taskkill /f /IM explorer.exe");
    close.waitFor();
    Runtime.getRuntime().exec("explorer.exe");
  }

}
