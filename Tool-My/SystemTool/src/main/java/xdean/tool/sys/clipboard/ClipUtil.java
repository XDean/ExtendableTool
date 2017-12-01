package xdean.tool.sys.clipboard;

import static xdean.jex.util.function.FunctionAdapter.supplierToRunnable;
import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import com.sun.javafx.application.PlatformImpl;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import xdean.jex.util.security.SecurityUtil;
import xdean.tool.api.Context;

public class ClipUtil {

  private static final Path TEMP_PATH = Context.TEMP_PATH.resolve("clip");
  private static Clipboard CLIPBOARD;
  static {
    try {
      if (Files.notExists(TEMP_PATH)) {
        Files.createDirectory(TEMP_PATH);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    PlatformImpl.startup(() -> CLIPBOARD = Clipboard.getSystemClipboard());
  }

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

  public static Optional<String> getClipText() {
    return getFX(() -> Optional.ofNullable(CLIPBOARD.getString()));
  }

  public static void setClipText(String string) {
    runFX(() -> {
      ClipboardContent content = new ClipboardContent();
      content.putString(string);
      CLIPBOARD.setContent(content);
    });
  }

  public static Optional<Image> getClipImage() {
    return getFX(() -> Optional.ofNullable(CLIPBOARD.getImage()));
  }

  public static void setClipImage(final Image image) {
    runFX(() -> {
      ClipboardContent content = new ClipboardContent();
      content.putImage(image);
      CLIPBOARD.setContent(content);
    });
  }

  public static String saveImage(BufferedImage image) throws IOException {
    String name = normalizeTextLength(String.format("Image%s.png", dateFormat.format(new Date())));
    ImageIO.write(image, "png", new File(TEMP_PATH.toString(), name));
    image.flush();
    return name;
  }

  public static Image loadImage(String name) throws IOException {
    BufferedImage swingImage = ImageIO.read(new File(TEMP_PATH.toString(), name));
    return SwingFXUtils.toFXImage(swingImage, null);
  }

  public static void cleanImage() {
    try {
      Files.newDirectoryStream(TEMP_PATH).forEach(path -> {
        if (path.getFileName().toString().matches("Image.*\\.jpg")) {
          try {
            Files.deleteIfExists(path);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String normalizeTextLength(String str) {
    if (str.length() < 30) {
      return str;
    }
    return str.substring(0, 25) + "...";
  }

  public static BufferedImage toBufferedImage(Image image) {
    return SwingFXUtils.fromFXImage(image, null);
  }

  public static String md5(BufferedImage image) {
    return uncheck(() -> {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(image, "png", os);
      InputStream fis = new ByteArrayInputStream(os.toByteArray());
      return SecurityUtil.md5(fis);
    });
  }

  private static <T> T getFX(Supplier<T> sup) {
    return supplierToRunnable(sup, PlatformImpl::runAndWait);
  }

  private static void runFX(Runnable r) {
    PlatformImpl.runAndWait(r);
  }
}
