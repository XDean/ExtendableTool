package xdean.screenShot;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import xdean.jex.config.Config;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolItem;

@Tool
public class ScreenShotTool extends AbstractToolItem {
  private final String ENABLE_KEY = "ScreenShotEnable";
  @Getter BooleanProperty enable;

  public ScreenShotTool() {
    super("");
    enable = new SimpleBooleanProperty(
        Config.getProperty(ENABLE_KEY)
            .map(Boolean::valueOf)
            .orElse(false));
    enable.addListener((ob, o, n) -> {
      ScreenShot.register(n);
      Config.setProperty(ENABLE_KEY, n.toString());
    });
    textProperty().bind(
        Bindings.when(enable)
            .then("Enable")
            .otherwise("Disable")
            .concat(" ScreenShot"));
  }

  @Override
  public void onClick() {
    enable.set(!enable.get());
  }
}
