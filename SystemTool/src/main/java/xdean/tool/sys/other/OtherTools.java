package xdean.tool.sys.other;

import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolItem;

@Tool
public class OtherTools extends AbstractToolItem {
  public OtherTools() {
    super("Other");
    childrenProperty().add(new ExplorerRestart());
  }

  @Override
  public void onClick() {
  }
}
