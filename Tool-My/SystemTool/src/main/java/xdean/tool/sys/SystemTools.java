package xdean.tool.sys;

import xdean.tool.api.IToolGetter;
import xdean.tool.api.Tool;
import xdean.tool.sys.other.CommandTool;

@Tool(path = "System")
public class SystemTools {

  @Tool
  public static final IToolGetter EXEPLORER_RESTART = () ->
      CommandTool.create("RestartExplorer", "cmd", "/c", "taskkill", "/f", "/IM", "explorer.exe", "&&", "explorer.exe");
}
