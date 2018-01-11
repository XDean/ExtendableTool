package xdean.screenShot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import xdean.jex.util.calc.MathUtil;
import xdean.jex.util.log.Logable;
import xdean.jex.util.task.If;
import xdean.jfx.ex.support.DragSupport;
import xdean.jfx.ex.support.DragSupport.DragConfig;
import xdean.jfx.ex.support.ResizeSupport;
import xdean.jfx.ex.support.ResizeSupport.ResizeConfig;

public class ScreenShotStage extends Stage implements Logable {
  Rectangle targetRectangle;
  Rectangle maskRectangle;
  ImageView mainImageView;
  Group rootGroup;
  Scene scene;
  HBox toolGroup;
  double[] startPos = new double[2];
  List<Rectangle2D> windowsBounds = new ArrayList<>();
  BooleanProperty userSelected = new SimpleBooleanProperty(false);

  Rectangle2D screenBound = Screen.getPrimary().getBounds();
  double screenWidth = screenBound.getWidth();
  double screenHeight = screenBound.getHeight();

  public ScreenShotStage() {
    super(StageStyle.TRANSPARENT);
    initComponent();
    initEvent();
    this.setScene(scene);
    this.setWidth(screenWidth);
    this.setHeight(screenHeight);
    this.setX(0);
    this.setY(0);
    this.setAlwaysOnTop(true);
  }

  public void reshot() {
    mainImageView.setImage(ScreenShot.getScreenShot());
    windowsBounds = JNAUtil.getWindowBounds();
  }

  public ScreenShotStage addTool(Node node) {
    toolGroup.getChildren().add(node);
    return this;
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
    WritableImage snapshot = mainImageView.snapshot(null, null);
    mainImageView.setClip(null);
    return snapshot;
  }

  private void updateMask() {
    maskRectangle.setClip(Shape.subtract(maskRectangle, targetRectangle));
  }

  private void initEvent() {
    DragConfig dragConfig = DragSupport.bind(targetRectangle);
    dragConfig.maxXProperty().bind(targetRectangle.widthProperty().subtract(screenWidth).negate());
    dragConfig.maxYProperty().bind(targetRectangle.heightProperty().subtract(screenHeight).negate());
    dragConfig.enableProperty().bind(userSelected);
    ResizeConfig resizeConfig = ResizeSupport.bind(targetRectangle);
    resizeConfig.maxWidthProperty().set(screenWidth);
    resizeConfig.maxHeightProperty().set(screenHeight);
    resizeConfig.defaultCursorProperty().bind(Bindings.when(userSelected)
        .then(Cursor.MOVE)
        .otherwise(Cursor.DEFAULT));

    userSelected.addListener((ob, o, n) -> targetRectangle.setCursor(n ? Cursor.MOVE : Cursor.DEFAULT));

    targetRectangle.setOnMouseDragged(e -> updateMask());
    scene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
      startPos[0] = e.getScreenX();
      startPos[1] = e.getScreenY();
    });
    scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
      userSelected.set(true);
      updateTargetRectangle(
          Math.min(e.getScreenX(), startPos[0]),
          Math.min(e.getScreenY(), startPos[1]),
          Math.abs(e.getScreenX() - startPos[0]),
          Math.abs(e.getScreenY() - startPos[1]));
    });
    scene.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> updateMask());
    scene.addEventHandler(MouseEvent.MOUSE_MOVED, e -> autoSelect(e));
    scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> If.that(e.getCode() == KeyCode.ESCAPE).todo(this::hide));
    scene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
      boolean inTarget = targetRectangle.contains(e.getScreenX() - targetRectangle.getLayoutX(), e.getScreenY() - targetRectangle.getLayoutY());
      if (userSelected.get()) {
        if (e.getButton() == MouseButton.SECONDARY) {
          reSelect(e);
        } else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() > 1) {
          confirmed();
        }
      } else if (e.getButton() == MouseButton.SECONDARY) {
        canceled();
      } else if (inTarget && e.getButton() == MouseButton.PRIMARY) {
        userSelected.set(true);
        if (e.getClickCount() > 1) {
          confirmed();
        } else {
          updateMask();
        }
      }
      e.consume();
    });

  }

  void autoSelect(MouseEvent e) {
    if (userSelected.get() == false) {
      windowsBounds.stream()
          .filter(r -> r.contains(e.getScreenX(), e.getScreenY()))
          .findFirst()
          .ifPresent(r -> updateTargetRectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight()));
    }
  }

  void canceled() {
    userSelected.set(false);
    hide();
  }

  void confirmed() {
    ScreenShot.putIntoClipBoard(getScreenShot());
    canceled();
  }

  void reSelect(MouseEvent e) {
    targetRectangle.setX(0);
    targetRectangle.setY(0);
    targetRectangle.setWidth(0);
    targetRectangle.setHeight(0);
    userSelected.set(false);
    autoSelect(e);
    updateMask();
  }

  private void updateTargetRectangle(double x, double y, double w, double h) {
    double newX = MathUtil.toRange(x, 0, screenWidth);
    double newY = MathUtil.toRange(y, 0, screenHeight);
    targetRectangle.setLayoutX(newX);
    targetRectangle.setLayoutY(newY);
    targetRectangle.setWidth(x + w - newX);
    targetRectangle.setHeight(y + h - newY);
    updateMask();
  }

  private void initComponent() {
    mainImageView = new ImageView();

    maskRectangle = new Rectangle();
    maskRectangle.setLayoutX(0);
    maskRectangle.setLayoutY(0);
    maskRectangle.setWidth(screenWidth);
    maskRectangle.setHeight(screenHeight);
    maskRectangle.setFill(new Color(0, 0, 0, 0.3));

    targetRectangle = new Rectangle();
    targetRectangle.setFill(Color.TRANSPARENT);
    targetRectangle.setStroke(Color.CYAN);
    targetRectangle.setStrokeType(StrokeType.CENTERED);
    targetRectangle.strokeWidthProperty().bind(Bindings.when(userSelected)
        .then(1).otherwise(5));

    toolGroup = new HBox();
    toolGroup.visibleProperty().bind(userSelected);
    toolGroup.layoutXProperty().bind(targetRectangle.layoutXProperty());
    DoubleBinding base = targetRectangle.layoutYProperty().add(targetRectangle.heightProperty());
    toolGroup.layoutYProperty().bind(Bindings.when(base.add(toolGroup.heightProperty()).greaterThan(screenHeight))
        .then(base.subtract(toolGroup.heightProperty()))
        .otherwise(base));

    rootGroup = new Group();
    scene = new Scene(rootGroup, 200, 200, Color.TRANSPARENT);
    rootGroup.getChildren().addAll(mainImageView, maskRectangle, targetRectangle, toolGroup);
  }
}
