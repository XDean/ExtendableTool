package xdean.screenShot;

import java.awt.Robot;
import java.awt.image.BufferedImage;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Screen;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import xdean.jex.config.Config;
import xdean.jex.util.task.TaskUtil;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import com.sun.javafx.application.PlatformImpl;

@UtilityClass
public class ScreenShot {
  private final String KEY = "ScreenShotKey";
  private final String DEFAULT_KEY = "ALT+SHIFT+A";
  private final HotkeyListener listener = e -> {
    if (e == 0) {
      show();
    }
  };
  private/* final */ScreenShotStage stage;
  static {
    TaskUtil.uncatch(() -> PlatformImpl.startup(() -> {
      PlatformImpl.setTaskbarApplication(false);
      Platform.setImplicitExit(false);
      stage = new ScreenShotStage();
    }));
    Config.setIfAbsent(KEY, DEFAULT_KEY);
  }

  public void main(String[] args) {
    // register();
    Platform.setImplicitExit(true);
    show();
  }

  public void register(boolean b) {
    if (b) {
      register();
    } else {
      unregister();
    }
  }

  public void register() {
    JIntellitype jni = JIntellitype.getInstance();
    jni.registerHotKey(0, Config.getProperty(KEY).orElse(DEFAULT_KEY));
    jni.addHotKeyListener(listener);
  }

  public void unregister() {
    JIntellitype jni = JIntellitype.getInstance();
    jni.unregisterHotKey(0);
    jni.removeHotKeyListener(listener);
  }

  public void show() {
    Platform.runLater(() -> {
      stage.reshot();
      stage.show();
    });
  }

  @SneakyThrows
  Image getScreenShot() {
    int width = (int) Screen.getPrimary().getBounds().getWidth();
    int height = (int) Screen.getPrimary().getBounds().getHeight();
    Robot robot = new Robot();
    BufferedImage swingImage = robot.createScreenCapture(new java.awt.Rectangle(width, height));
    return SwingFXUtils.toFXImage(swingImage, new WritableImage(width, height));
  }

  void putIntoClipBoard(Image image) {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putImage(image);
    clipboard.setContent(content);
  }
}
