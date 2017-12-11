package xdean.screenShot;

import static xdean.jex.util.lang.ExceptionUtil.throwToReturn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import javax.imageio.ImageIO;

import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import xdean.jex.util.task.If;
import xdean.jfx.ex.util.LayoutUtil;
import xdean.tool.api.Config;

public class ScreenShotToolBar {

  private static final String KEY_SCREEN_SHOT__SAVE = "ScreenShotLastSavePath";
  private static final File DEFAULT_SAVE_DIR = new File(System.getProperty("user.home"));

  public static ScreenShotStage pixelSize(ScreenShotStage stage) {
    Text text = new Text();
    text.textProperty().bind(
        Bindings.concat("(")
            .concat(stage.targetRectangle.widthProperty().asString("%.0f"))
            .concat(", ")
            .concat(stage.targetRectangle.heightProperty().asString("%.0f"))
            .concat(")"));
    HBox hbox = new HBox(text);
    text.setTextAlignment(TextAlignment.JUSTIFY);
    text.setFill(Color.WHITE);
    hbox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
    hbox.setAlignment(Pos.CENTER);
    LayoutUtil.margin(text, 5, 5);
    return stage.addTool(hbox);
  }

  public static ScreenShotStage saveButton(ScreenShotStage stage) {
    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    FileChooser fc = new FileChooser();
    ExtensionFilter jpg = new ExtensionFilter("jpg", Collections.singletonList("*.jpg"));
    ExtensionFilter png = new ExtensionFilter("png", Collections.singletonList("*.png"));
    ExtensionFilter bmp = new ExtensionFilter("bmp", Collections.singletonList("*.bmp"));
    fc.getExtensionFilters().add(jpg);
    fc.getExtensionFilters().add(png);
    fc.getExtensionFilters().add(bmp);
    fc.setTitle("Save");
    return stage.addToolButton("save", s -> {
      s.setAlwaysOnTop(false);
      fc.setInitialFileName("ScreenShot" + df.format(new Date()));
      fc.setInitialDirectory(
          Config.getProperty(KEY_SCREEN_SHOT__SAVE)
              .map(File::new)
              .filter(File::exists)
              .orElse(DEFAULT_SAVE_DIR));
      BufferedImage screenShot = SwingFXUtils.fromFXImage(s.getScreenShot(), null);
      Optional.ofNullable(fc.showSaveDialog(s.getOwner()))
          .ifPresent(file -> throwToReturn(
              () -> {
                Config.setProperty(KEY_SCREEN_SHOT__SAVE, file.getParentFile().getAbsolutePath());
                ExtensionFilter filter = fc.getSelectedExtensionFilter();
                BufferedImage image = null;
                if (filter == jpg) {
                  image = toJPG(screenShot);
                } else if (filter == bmp) {
                  image = toBMP(screenShot);
                } else if (filter == png) {
                  image = toPNG(screenShot);
                }
                return ImageIO.write(image, filter.getDescription(), file);
              })
                  .ifRight(e -> s.log().debug("write screenshot fail. Retry.", e))
                  .unify(f -> If.that(f), e -> If.that(false))
                  .todo(() -> s.reshot())
                  .todo(() -> s.hide())
                  .ordo(() -> new Alert(AlertType.INFORMATION, "Save failed!", ButtonType.OK).showAndWait()));
      s.setAlwaysOnTop(true);
    });
  }

  private static BufferedImage toPNG(BufferedImage image) {
    return toFormat(image, BufferedImage.TYPE_INT_ARGB_PRE);
  }

  private static BufferedImage toJPG(BufferedImage image) {
    return toFormat(image, BufferedImage.TYPE_INT_RGB);
  }

  private static BufferedImage toBMP(BufferedImage image) {
    return toFormat(image, BufferedImage.TYPE_INT_RGB);
  }

  private static BufferedImage toFormat(BufferedImage image, int format) {
    if (image.getType() == format) {
      return image;
    }
    BufferedImage newBufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), format);
    newBufferedImage.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);
    return newBufferedImage;
  }
}
