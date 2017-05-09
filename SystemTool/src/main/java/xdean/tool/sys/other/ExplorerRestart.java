package xdean.tool.sys.other;

import xdean.tool.sys.CommandTool;

public class ExplorerRestart extends CommandTool {
  public ExplorerRestart() {
    super("taskkill /f /IM explorer.exe && explorer.exe");
    textProperty().set("RestartExplorer");
  }
}
