package xdean.tool.other;

import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Random;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolItem;

@Tool(parent = OtherTool.class)
public class ScrrenProtection extends AbstractToolItem implements Runnable {

  private final BooleanProperty enable = new SimpleBooleanProperty(this, "enable");
  private final Random RANDOM = new Random();
  private final Robot r;
  private Disposable d = Disposables.disposed();

  public ScrrenProtection() throws AWTException {
    r = new Robot();
    textProperty().bind(Bindings.when(enable).then("Disable Screen Protection").otherwise("Enable Screen Protection"));
    enable.addListener((ob, o, n) -> {
      if (n) {
        d = Schedulers.newThread().scheduleDirect(this);
      } else {
        d.dispose();
      }
    });
  }

  @Override
  public void onClick() {
    enable.set(!enable.get());
    if (enable.get()) {
    }
  }

  @Override
  public void run() {
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    int oldX = 0;
    int oldY = 0;
    int dx = random();
    int dy = random();
    while (true) {
      Point location = MouseInfo.getPointerInfo().getLocation();
      int x = location.x;
      int y = location.y;
      if (x != oldX || y != oldY) {
        oldX = x;
        oldY = y;
        sleep(60000);
        continue;
      }
      if (x + dx < 0) {
        dx = random();
      } else if (x + dx >= size.width) {
        dx = -random();
      }
      if (y + dy < 0) {
        dy = random();
      } else if (y + dy >= size.height) {
        dy = -random();
      }
      r.mouseMove(x += dx, y += dy);
      oldX = x;
      oldY = y;
      sleep(15);
    }
  }

  private void sleep(int mills) {
    uncheck(() -> Thread.sleep(mills));
  }

  private int random() {
    return RANDOM.nextInt(30) + 1;
  }
}
