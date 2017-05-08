package xdean.tool.sys.other;

import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolMenu;

@Tool
public class OtherTools extends AbstractToolMenu {
  public OtherTools() {
    super("Other");
    childrenProperty().add(new ExplorerRestart());
  }

  @Override
  public void onClick() {
  }
}
