package xdean.screenShot;

import static xdean.jex.util.cache.CacheUtil.cache;
import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import com.sun.javafx.application.PlatformImpl;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Screen;
import xdean.tool.api.Config;

public class ScreenShot {
  private static AtomicBoolean registered = new AtomicBoolean(false);
  static final int CODE = 0x951107;
  static final String KEY = "ScreenShotKey";
  static final String DEFAULT_KEY = "ALT+SHIFT+A";
  static final HotkeyListener LISTENER = e -> {
    if (e == CODE) {
      show();
    }
  };
  private static /* final */ScreenShotStage stage;
  static {
    PlatformImpl.startup(() -> {
      PlatformImpl.setTaskbarApplication(false);
      Platform.setImplicitExit(false);
    });
    Config.setIfAbsent(KEY, DEFAULT_KEY);
  }

  public static void main(String[] args) {
    // register();
    Platform.setImplicitExit(true);
    show();
    Platform.runLater(() -> {
      initStage();
      stage.setOnHidden(e -> System.exit(0));
    });
  }

  public static String getKey() {
    return Config.getProperty(KEY).orElse(DEFAULT_KEY);
  }

  public static void setKey(String value) {
    Config.setProperty(KEY, value);
    if (registered.get()) {
      synchronized (registered) {
        if (registered.get()) {
          unregister();
          register();
        }
      }
    }
  }

  public static void register(boolean b) {
    if (b) {
      register();
    } else {
      unregister();
    }
  }

  public static void register() {
    if (registered.get() == false) {
      synchronized (registered) {
        if (registered.compareAndSet(false, true)) {
          JIntellitype.getInstance().registerHotKey(CODE, getKey());
          JIntellitype.getInstance().addHotKeyListener(LISTENER);
        }
      }
    }
  }

  public static void unregister() {
    if (registered.get()) {
      synchronized (registered) {
        if (registered.compareAndSet(true, false)) {
          JIntellitype.getInstance().unregisterHotKey(CODE);
          JIntellitype.getInstance().removeHotKeyListener(LISTENER);
        }
      }
    }
  }

  public static void show() {
    Platform.runLater(() -> {
      initStage();
      stage.reshot();
      stage.show();
    });
  }

  private static void initStage() {
    if (stage == null) {
      stage = new ScreenShotStage();
      ScreenShotToolBar.pixelSize(stage);
      ScreenShotToolBar.saveButton(stage);
    }
  }

  static Image getScreenShot() {
    int width = (int) Screen.getPrimary().getBounds().getWidth();
    int height = (int) Screen.getPrimary().getBounds().getHeight();
    BufferedImage swingImage = cache(ScreenShot.class, Robot.class, () -> uncheck(() -> new Robot()))
        .createScreenCapture(new java.awt.Rectangle(width, height));
    return SwingFXUtils.toFXImage(swingImage, new WritableImage(width, height));
  }

  static void putIntoClipBoard(Image image) {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putImage(image);
    clipboard.setContent(content);
  }
}
