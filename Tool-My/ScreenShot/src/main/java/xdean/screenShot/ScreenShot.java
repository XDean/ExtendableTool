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
    PlatformImpl.startup(() -> {
      PlatformImpl.setTaskbarApplication(false);
      Platform.setImplicitExit(false);
    });
    Config.setIfAbsent(KEY, DEFAULT_KEY);
  }

  public void main(String[] args) {
    // register();
    Platform.setImplicitExit(true);
    show();
    Platform.runLater(() -> {
      initStage();
      stage.setOnHidden(e -> System.exit(0));
    });
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
      initStage();
      stage.reshot();
      stage.show();
    });
  }

  private void initStage() {
    if (stage == null) {
      stage = new ScreenShotStage();
      stage.addToolButton("1", s -> System.out.println(1));
      stage.addToolButton("2", s -> System.out.println(2));
      stage.addToolButton("3", s -> System.out.println(3));
    }
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
