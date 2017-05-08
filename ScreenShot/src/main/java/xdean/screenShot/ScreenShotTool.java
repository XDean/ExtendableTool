package xdean.screenShot;

import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolMenu;

@Tool
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
