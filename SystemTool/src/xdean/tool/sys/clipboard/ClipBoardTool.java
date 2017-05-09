package xdean.tool.sys.clipboard;

import io.reactivex.internal.schedulers.RxThreadFactory;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Scheduler;
import rx.plugins.RxJavaSchedulersHook;
import xdean.jex.util.task.TaskUtil;
import xdean.tool.api.ITool;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolItem;
import xdean.tool.api.impl.SeparatorItem;
import xdean.tool.api.impl.SimpleToolItem;

@Tool
public class ClipBoardTool extends AbstractToolItem {

  private Map<String, ITool> stringMap;
  private Map<String, ITool> imageMap;

  private ITool clearItem;

  public ClipBoardTool() {
    super("Clip Board");
    stringMap = new HashMap<>();
    imageMap = new HashMap<>();
    Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(e -> newContent());

    clearItem = new SimpleToolItem("Clear", () -> {
      childrenProperty().clear();
      stringMap.clear();
      childrenProperty().add(clearItem);
      childrenProperty().add(SeparatorItem.INSTANCE);
      ClipUtil.cleanImage();
      System.gc();
    });
    clearItem.onClick();
  }

  static Scheduler scheduler = RxJavaSchedulersHook.createIoScheduler(new RxThreadFactory("ClipBoard-", 10));

  private void newContent() {
    System.out.println("newContent");
    Observable
        .just(1)
        .observeOn(scheduler)
        .subscribe(o -> {
          ClipUtil.getClipText().ifPresent(
              str -> {
                if (stringMap.containsKey(str)) {
                  ITool item = stringMap.get(str);
                  childrenProperty().remove(item);
                  childrenProperty().add(item);
                } else {
                  ITool item = new SimpleToolItem(ClipUtil.normalizeTextLength(str),
                      () -> ClipUtil.setClipText(str));
                  childrenProperty().add(item);
                  stringMap.put(str, item);
                }
              });
          ClipUtil.getClipImage().ifPresent(image -> TaskUtil.uncatch(() -> {
            BufferedImage bImage = ClipUtil.toBufferedImage(image);
            String md5 = ClipUtil.md5(bImage);
            if (imageMap.containsKey(md5)) {
              ITool item = imageMap.get(md5);
              childrenProperty().remove(item);
              childrenProperty().add(item);
            } else {
              ITool item = new ClipImage(image, ClipUtil.saveImage(bImage));
              childrenProperty().add(item);
              imageMap.put(md5, item);
            }
          }));
        }, e -> e.printStackTrace());
  }

  @Override
  public void onClick() {

  }
}