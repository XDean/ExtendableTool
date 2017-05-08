package xdean.screenShot;

import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolItem;

@Tool
public class ScreenShotTool extends AbstractToolItem {
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
