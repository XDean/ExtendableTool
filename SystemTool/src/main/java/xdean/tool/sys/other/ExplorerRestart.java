package xdean.tool.sys.other;

import xdean.tool.api.Tool;
import xdean.tool.sys.SystemTools;

@Tool(parent = SystemTools.class)
public class ExplorerRestart extends CommandTool {
  public ExplorerRestart() {
    super("taskkill /f /IM explorer.exe && explorer.exe");
    textProperty().set("RestartExplorer");
  }
}
