package xdean.tool.api;

import java.util.Optional;

import lombok.experimental.UtilityClass;
import rx.Observable;
import rx.functions.Func1;
import xdean.jex.util.string.StringUtil;
import xdean.tool.api.impl.TextToolItem;

@UtilityClass
public class ToolUtil {

  public Optional<ITool> getTool(Class<?> clz) {
    Tool toolAnnotation = clz.getAnnotation(Tool.class);
    if (toolAnnotation == null) {
      return Optional.empty();
    }
    Object newInstance;
    try {
      newInstance = clz.newInstance();
    } catch (Exception e) {
      return Optional.empty();
    }
    if (newInstance instanceof ITool) {
      return Optional.of((ITool) newInstance);
    } else {
      return Optional.empty();
    }
  }

  public Optional<ITool> getWrappedTool(Class<?> clz) {
    return getTool(clz).map(ToolUtil::wrapTool);
  }

  private <T extends ITool> ITool wrapTool(ITool tool) {
    return wrapTool(tool, TextToolItem::new);
  }

  private <T extends ITool> ITool wrapTool(ITool tool, Func1<String, ITool> func) {
    return Observable.just(tool.getClass())
        .map(ToolUtil::getToolPath)
        .flatMap(p -> Observable.from(p.split("/")))
        .filter(s -> s.length() > 0)
        .map(func)
        .concatWith(Observable.just(tool))
        .scan((a, b) -> {
          a.childrenProperty().add(b);
          return b;
        })
        .toList()
        .map(l -> l.get(0))
        .toBlocking()
        .first();
  }

  public String getToolPath(Class<?> clz) {
    Tool tool = clz.getAnnotation(Tool.class);
    if (tool == null) {
      return "";
    }
    return String.join("/", getToolPath(tool.parent()), StringUtil.unWrap(tool.path(), "/", ""));
  }
}
