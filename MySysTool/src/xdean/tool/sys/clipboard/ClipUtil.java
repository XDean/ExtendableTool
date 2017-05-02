package xdean.tool.sys.clipboard;

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

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import javax.imageio.ImageIO;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import xdean.jex.extra.Wrapper;
import xdean.jex.util.security.SecurityUtil;
import xdean.tool.api.Context;

import com.sun.javafx.application.PlatformImpl;

@UtilityClass
public class ClipUtil {

  private final Path TEMP_PATH = Context.TEMP_PATH.resolve("clip");
  private Clipboard CLIPBOARD;
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

  private SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSSS");

  public Optional<String> getClipText() {
    return getFX(() -> Optional.ofNullable(CLIPBOARD.getString()));
  }

  public void setClipText(String string) {
    runFX(() -> {
      ClipboardContent content = new ClipboardContent();
      content.putString(string);
      CLIPBOARD.setContent(content);
    });
  }

  public Optional<Image> getClipImage() {
    return getFX(() -> Optional.ofNullable(CLIPBOARD.getImage()));
  }

  public void setClipImage(final Image image) {
    runFX(() -> {
      ClipboardContent content = new ClipboardContent();
      content.putImage(image);
      CLIPBOARD.setContent(content);
    });
  }

  public String saveImage(BufferedImage image) throws IOException {
    String name = ClipUtil.normalizeTextLength(String.format("Image%s.png", dateFormat.format(new Date())));
    ImageIO.write(image, "png", new File(TEMP_PATH.toString(), name));
    image.flush();
    return name;
  }

  public Image loadImage(String name) throws IOException {
    BufferedImage swingImage = ImageIO.read(new File(TEMP_PATH.toString(), name));
    return SwingFXUtils.toFXImage(swingImage, null);
  }

  public void cleanImage() {
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

  public String normalizeTextLength(String str) {
    if (str.length() < 30) {
      return str;
    }
    return str.substring(0, 25) + "...";
  }

  public BufferedImage toBufferedImage(Image image) {
    return SwingFXUtils.fromFXImage(image, null);
  }

  @SneakyThrows(IOException.class)
  public String md5(BufferedImage image) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(image, "png", os);
    InputStream fis = new ByteArrayInputStream(os.toByteArray());
    return SecurityUtil.md5(fis);
  }

  private <T> T getFX(Supplier<T> sup) {
    Wrapper<T> wrapper = Wrapper.of(null);
    PlatformImpl.runAndWait(() -> wrapper.set(sup.get()));
    return wrapper.get();
  }

  private void runFX(Runnable r) {
    PlatformImpl.runAndWait(r);
  }
}
