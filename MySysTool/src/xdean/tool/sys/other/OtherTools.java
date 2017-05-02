package xdean.tool.sys.other;

import xdean.tool.api.impl.AbstractToolMenu;
import xdean.tool.api.inter.ToolMenu;

@ToolMenu
public class OtherTools extends AbstractToolMenu {
  public OtherTools() {
    super("Other");
    childrenProperty().add(new ExplorerRestart());
  }

  @Override
  public void onClick() {
  };
}
