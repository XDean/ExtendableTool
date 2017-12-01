package xdean.tool.sys.clipboard;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import xdean.jex.util.lang.ExceptionUtil;
import xdean.jex.util.log.Logable;
import xdean.tool.api.ITool;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.SeparatorItem;
import xdean.tool.api.impl.SimpleToolItem;
import xdean.tool.api.impl.TextToolItem;
import xdean.tool.sys.SystemTools;

@Tool(parent = SystemTools.class)
public class ClipBoardTool extends TextToolItem implements Logable {

  private Map<String, ITool> stringMap;
  private Map<String, ITool> imageMap;

  private ITool clearItem;

  public ClipBoardTool() {
    super("Clip Board");
    stringMap = new HashMap<>();
    imageMap = new HashMap<>();
    Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(e -> newContent());

    clearItem = new SimpleToolItem("Clear", () -> {
      new ArrayList<>(childrenProperty()).forEach(t -> t.removeFromParent());
      stringMap.clear();
      addChild(clearItem);
      addChild(SeparatorItem.INSTANCE);
      ClipUtil.cleanImage();
      System.gc();
    });
    clearItem.onClick();
  }

  private void newContent() {
    log().debug("New content comes to clip board.");
    Observable
        .just(1)
        .observeOn(Schedulers.io())
        .doOnNext(o -> ClipUtil.getClipText().ifPresent(str -> {
          if (stringMap.containsKey(str)) {
            ITool item = stringMap.get(str);
            removeChild(item);
            addChild(item);
          } else {
            ITool item = new SimpleToolItem(ClipUtil.normalizeTextLength(str),
                () -> ClipUtil.setClipText(str));
            addChild(item);
            stringMap.put(str, item);
          }
        }))
        .doOnNext(o -> ClipUtil.getClipImage().ifPresent(image -> ExceptionUtil.uncatch(() -> {
          BufferedImage bImage = ClipUtil.toBufferedImage(image);
          String md5 = ClipUtil.md5(bImage);
          if (imageMap.containsKey(md5)) {
            ITool item = imageMap.get(md5);
            removeChild(item);
            addChild(item);
          } else {
            ITool item = new ClipImage(image, ClipUtil.saveImage(bImage));
            addChild(item);
            imageMap.put(md5, item);
          }
        })))
        .doOnError(e -> log().error("", e))
        .subscribe();
  }
}
