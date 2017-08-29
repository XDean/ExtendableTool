package xdean.tool.sys.other;

import xdean.tool.api.IToolGetter;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.SimpleToolItem;
import xdean.tool.sys.SystemTools;

@Tool(parent = SystemTools.class, path = "Custom")
public class CustomCommandTool {
  @Tool
  public static final IToolGetter tool = () -> new SimpleToolItem("Help", () -> System.out.println(123));
}
