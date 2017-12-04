package xdean.screenShot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import xdean.jex.util.log.Logable;
import xdean.tool.api.Config;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolItem;

@Tool(path = "ScreenShot")
public class ScreenShotTool {
  @Tool
  public static class ScreenShotEnable extends AbstractToolItem implements Logable {
    private final String ENABLE_KEY = "ScreenShotEnable";
    BooleanProperty enable;

    public ScreenShotEnable() {
      super();
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

  @Tool
  public static class ScreenShotKey extends AbstractToolItem {

    public ScreenShotKey() {
      super(ScreenShot.getKey());
    }

    @Override
    public void onClick() {
      Platform.runLater(() -> {
        TextInputDialog in = new TextInputDialog(ScreenShot.getKey());
        in.setHeaderText("Set screen shot shortcut");
        in.setContentText("Select the input field and press the new shorcut. Only ALT/SHIFT/CTRL + A~Z is legal.");
        TextField editor = in.getEditor();
        editor.setEditable(false);
        editor.setOnKeyPressed(e -> {
          List<String> list = new ArrayList<>();
          if (e.isAltDown()) {
            list.add("ALT");
          }
          if (e.isShiftDown()) {
            list.add("SHIFT");
          }
          if (e.isControlDown()) {
            list.add("CTRL");
          }
          if (e.getCode().isLetterKey()) {
            list.add(e.getCode().toString());
          } else {
            list.add("");
          }
          editor.setText(list.stream().collect(Collectors.joining(" + ")));
          e.consume();
        });
        in.showAndWait().ifPresent(s -> {
          ScreenShot.setKey(Arrays.stream(s.split(" \\+ ")).collect(Collectors.joining("+")));
          textProperty().set(s);
        });
      });
    }
  }
}
