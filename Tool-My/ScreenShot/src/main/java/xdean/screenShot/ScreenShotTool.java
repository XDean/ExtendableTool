package xdean.screenShot;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import xdean.jex.util.log.Logable;
import xdean.tool.api.Config;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolItem;

@Tool
public class ScreenShotTool extends AbstractToolItem implements Logable {
  private final String ENABLE_KEY = "ScreenShotEnable";
  BooleanProperty enable;

  public ScreenShotTool() {
    super("");
    enable = new SimpleBooleanProperty(false);
    enable.addListener((ob, o, n) -> {
      ScreenShot.register(n);
      Config.setProperty(ENABLE_KEY, n.toString());
      log().debug((n ? "Enable" : "Disable") + "Screen Shot");
    });
    enable.set(Config.getProperty(ENABLE_KEY).map(Boolean::valueOf).orElse(false));
    textProperty().bind(Bindings.when(enable)
        .then("Disable")
        .otherwise("Enable")
        .concat(" ScreenShot"));
  }

  @Override
  public void onClick() {
    enable.set(!enable.get());
  }
}
