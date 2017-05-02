package xdean.tool.sys.clipboard;

import java.io.IOException;

import javafx.scene.image.Image;
import xdean.tool.api.impl.AbstractToolMenu;

class ClipImage extends AbstractToolMenu {

  private String imageName;

  public ClipImage(Image image, String name) throws IOException {
    super();
    this.imageName = name;
    textProperty().set(imageName);
  }

  @Override
  public void onClick() {
    try {
      Image image = ClipUtil.loadImage(imageName);
      ClipUtil.setClipImage(image);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}