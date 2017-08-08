package xdean.screenShot;

import java.util.function.Consumer;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
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
import xdean.jfx.ex.support.DragSupport;

public class ScreenShotStage extends Stage {
  private Rectangle targetRectangle;
  private Rectangle maskRectangle;
  private ImageView mainImageView;
  private Group rootGroup;
  private Scene scene;

  public ScreenShotStage() {
    super(StageStyle.TRANSPARENT);
    initScene();
    initRectangle();
    initImageView();
    initEvent();
    this.setScene(scene);
    this.setWidth(Screen.getPrimary().getBounds().getWidth());
    this.setHeight(Screen.getPrimary().getBounds().getHeight());
    this.setX(0);
    this.setY(0);
    this.setAlwaysOnTop(true);
  }

  public void reshot() {
    mainImageView.setImage(ScreenShot.getScreenShot());
  }

  public ScreenShotStage addToolButton(String text, Consumer<ScreenShotStage> onClick) {
    // TODO
    return this;
  }

  private void initEvent() {
    double[] startPos = new double[2];
    scene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
      startPos[0] = e.getScreenX();
      startPos[1] = e.getScreenY();
      targetRectangle.setWidth(0);
      targetRectangle.setHeight(0);
    });
    scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
      targetRectangle.setLayoutX(Math.min(e.getScreenX(), startPos[0]));
      targetRectangle.setLayoutY(Math.min(e.getScreenY(), startPos[1]));
      targetRectangle.setWidth(Math.abs(e.getScreenX() - startPos[0]));
      targetRectangle.setHeight(Math.abs(e.getScreenY() - startPos[1]));
      maskRectangle.setClip(Shape.subtract(maskRectangle, targetRectangle));
    });
    scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        this.hide();
      }
    });
    scene.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        e -> {
          if (targetRectangle.contains(e.getScreenX() - targetRectangle.getLayoutX(),
              e.getScreenY() - targetRectangle.getLayoutY())) {
            if (e.getClickCount() > 1) {
              Shape shape = Shape.union(targetRectangle, new Rectangle());
              mainImageView.setClip(shape);
              ScreenShot.putIntoClipBoard(mainImageView.snapshot(null, null));
              this.hide();
            } else if (e.getButton() == MouseButton.SECONDARY) {
              targetRectangle.setWidth(0);
              targetRectangle.setHeight(0);
              maskRectangle.setClip(Shape.subtract(maskRectangle, targetRectangle));
            }
          } else {
            if (e.getButton() == MouseButton.SECONDARY || e.getClickCount() > 1) {
              e.consume();
              this.hide();
            }
          }
        });
  }

  private void initImageView() {
    mainImageView = new ImageView();
    rootGroup.getChildren().addAll(mainImageView, maskRectangle, targetRectangle);
  }

  private void initScene() {
    rootGroup = new Group();
    scene = new Scene(rootGroup, 200, 200, Color.TRANSPARENT);
  }

  private void initRectangle() {
    targetRectangle = new Rectangle();
    targetRectangle.setFill(Color.TRANSPARENT);
    targetRectangle.setStroke(Color.BLACK);

    maskRectangle = new Rectangle();
    maskRectangle.setLayoutX(0);
    maskRectangle.setLayoutY(0);
    maskRectangle.setWidth(Screen.getPrimary().getBounds().getWidth());
    maskRectangle.setHeight(Screen.getPrimary().getBounds().getHeight());
    maskRectangle.setFill(new Color(0, 0, 0, 0.3));

    DragSupport.bind(targetRectangle)
        .doOnDrag(() -> maskRectangle.setClip(Shape.subtract(maskRectangle, targetRectangle)));
  }
}