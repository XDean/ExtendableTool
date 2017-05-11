package xdean.tool.api.toolUtil;

import xdean.tool.api.ITool;
import xdean.tool.api.Tool;

@Tool
public class WithMethod {

  @Tool
  public static ITool legal() {
    return new EmptyToolItem();
  }

  @Tool
  public static Object illegal1() {
    return null;
  }

  @Tool
  static ITool illegal2() {
    return new EmptyToolItem();
  }

  @Tool
  public static ITool illegal3() {
    return null;
  }

  public static ITool illegal4() {
    return new EmptyToolItem();
  }
}
