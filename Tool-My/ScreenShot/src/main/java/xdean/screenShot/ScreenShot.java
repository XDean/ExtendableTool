package xdean.screenShot;

import java.awt.Robot;
import java.awt.image.BufferedImage;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import xdean.jex.config.Config;
import xdean.jex.util.task.TaskUtil;
import xdean.jfx.ex.support.DragSupport;

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

  static {
    TaskUtil.uncatch(() -> PlatformImpl.startup(() -> {
      PlatformImpl.setTaskbarApplication(false);
      Platform.setImplicitExit(false);
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

  // FIXME unregister and register again will lead double listener
  public void register() {
    JIntellitype jni = JIntellitype.getInstance();
    jni.registerHotKey(0, Config.getProperty(KEY).orElse(DEFAULT_KEY));
    jni.addHotKeyListener(listener);
  }

  public void unregister() {
    JIntellitype jni = JIntellitype.getInstance();
    jni.unregisterHotKey(0);
  }

  // TODO reuse it
  public void show() {
    Platform.runLater(() -> {
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      stage.setAlwaysOnTop(true);
      Group rootGroup = new Group();
      Scene scene = new Scene(rootGroup, 200, 200, Color.TRANSPARENT);

      Rectangle target = new Rectangle();
      target.setFill(Color.TRANSPARENT);
      target.setStroke(Color.BLACK);

      Rectangle mask = new Rectangle();
      mask.setLayoutX(0);
      mask.setLayoutY(0);
      mask.setWidth(Screen.getPrimary().getBounds().getWidth());
      mask.setHeight(Screen.getPrimary().getBounds().getHeight());
      mask.setFill(new Color(0, 0, 0, 0.3));

      DragSupport.bind(target).doOnDrag(() -> mask.setClip(Shape.subtract(mask, target)));

      ImageView view = new ImageView(getScreenShot());
      rootGroup.getChildren().addAll(view, mask, target);

      double[] startPos = new double[2];
      scene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
        startPos[0] = e.getScreenX();
        startPos[1] = e.getScreenY();
        target.setWidth(0);
        target.setHeight(0);
      });
      scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
        target.setLayoutX(Math.min(e.getScreenX(), startPos[0]));
        target.setLayoutY(Math.min(e.getScreenY(), startPos[1]));
        target.setWidth(Math.abs(e.getScreenX() - startPos[0]));
        target.setHeight(Math.abs(e.getScreenY() - startPos[1]));
        mask.setClip(Shape.subtract(mask, target));
      });
      scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
          stage.close();
        }
      });
      scene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
        if (target.contains(e.getScreenX() - target.getLayoutX(), e.getScreenY() - target.getLayoutY())) {
          if (e.getClickCount() > 1) {
            Shape shape = Shape.union(target, new Rectangle());
            view.setClip(shape);
            putIntoClipBoard(view.snapshot(null, null));
            stage.close();
          } else if (e.getButton() == MouseButton.SECONDARY) {
            target.setWidth(0);
            target.setHeight(0);
            mask.setClip(Shape.subtract(mask, target));
          }
        } else {
          if (e.getButton() == MouseButton.SECONDARY || e.getClickCount() > 1) {
            e.consume();
            stage.close();
          }
        }
      });

      stage.setScene(scene);
      stage.setWidth(Screen.getPrimary().getBounds().getWidth());
      stage.setHeight(Screen.getPrimary().getBounds().getHeight());
      stage.setX(0);
      stage.setY(0);
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
