package xdean.screenShot;

import java.util.Arrays;
import java.util.function.Consumer;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;

import xdean.jex.util.log.Logable;
import xdean.jex.util.task.If;
import xdean.jfx.ex.support.DragSupport;

public class ScreenShotStage extends Stage implements Logable {
  public static void main(String[] args) {
    System.out.println(Arrays.toString(ImageIO.getWriterFormatNames()));
  }

  private Rectangle targetRectangle;
  private Rectangle maskRectangle;
  private ImageView mainImageView;
  private Group rootGroup;
  private Scene scene;
  double[] startPos = new double[2];
  private HBox toolGroup;

  public ScreenShotStage() {
    super(StageStyle.TRANSPARENT);
    initComponent();
    initEvent();
    this.setScene(scene);
    this.setWidth(Screen.getPrimary().getBounds().getWidth());
    this.setHeight(Screen.getPrimary().getBounds().getHeight());
    this.setX(0);
    this.setY(0);
    this.setAlwaysOnTop(true);
  }

  @Override
  public void hide() {
    super.hide();
    targetRectangle.setWidth(0);
    targetRectangle.setHeight(0);
    updateMask();
  }

  public void reshot() {
    mainImageView.setImage(ScreenShot.getScreenShot());
  }

  public ScreenShotStage addToolButton(String text, Consumer<ScreenShotStage> onClick) {
    Button button = new Button();
    button.setText(text);
    button.setOnMouseClicked(e -> If.that(e.getButton() == MouseButton.PRIMARY).todo(() -> onClick.accept(this)));
    toolGroup.getChildren().add(button);
    return this;
  }

  public Image getScreenShot() {
    Shape shape = Shape.union(targetRectangle, new Rectangle());
    mainImageView.setClip(shape);
    return mainImageView.snapshot(null, null);
  }

  private void updateMask() {
    maskRectangle.setClip(Shape.subtract(maskRectangle, targetRectangle));
  }

  private void initEvent() {
    DragSupport.bind(targetRectangle).doOnDrag(this::updateMask);

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
      updateMask();
    });
    scene.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> updateMask());
    scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> If.that(e.getCode() == KeyCode.ESCAPE).todo(this::hide));
    scene.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        e -> {
          if (targetRectangle.contains(e.getScreenX() - targetRectangle.getLayoutX(),
              e.getScreenY() - targetRectangle.getLayoutY())) {
            if (e.getClickCount() > 1) {
              ScreenShot.putIntoClipBoard(getScreenShot());
              e.consume();
              this.hide();
            } else if (e.getButton() == MouseButton.SECONDARY) {
              targetRectangle.setWidth(0);
              targetRectangle.setHeight(0);
              updateMask();
            }
          } else {
            if (e.getButton() == MouseButton.SECONDARY || e.getClickCount() > 1) {
              e.consume();
              this.hide();
            }
          }
        });
  }

  private void initComponent() {
    mainImageView = new ImageView();

    maskRectangle = new Rectangle();
    maskRectangle.setLayoutX(0);
    maskRectangle.setLayoutY(0);
    maskRectangle.setWidth(Screen.getPrimary().getBounds().getWidth());
    maskRectangle.setHeight(Screen.getPrimary().getBounds().getHeight());
    maskRectangle.setFill(new Color(0, 0, 0, 0.3));

    targetRectangle = new Rectangle();
    targetRectangle.setFill(Color.TRANSPARENT);
    targetRectangle.setStroke(Color.BLACK);

    toolGroup = new HBox();
    toolGroup.visibleProperty().bind(targetRectangle.widthProperty().greaterThan(0));
    toolGroup.layoutXProperty().bind(targetRectangle.layoutXProperty());
    toolGroup.layoutYProperty().bind(targetRectangle.layoutYProperty().add(targetRectangle.heightProperty()));

    rootGroup = new Group();
    scene = new Scene(rootGroup, 200, 200, Color.TRANSPARENT);
    rootGroup.getChildren().addAll(mainImageView, maskRectangle, targetRectangle, toolGroup);
  }
}
