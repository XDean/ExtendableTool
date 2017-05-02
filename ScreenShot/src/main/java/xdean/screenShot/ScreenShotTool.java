package xdean.screenShot;

import xdean.tool.api.impl.AbstractToolMenu;
import xdean.tool.api.inter.ToolMenu;

@ToolMenu
public class ScreenShotTool extends AbstractToolMenu {
  boolean enable;

  public ScreenShotTool() {
    super("Enable ScreenShot");
  }

  @Override
  public void onClick() {
    if (enable) {
      ScreenShot.unregister();
      text.set("Enable ScreenShot");
    } else {
      ScreenShot.register();
      text.set("Disable ScreenShot");
    }
    enable = !enable;
  }
}
