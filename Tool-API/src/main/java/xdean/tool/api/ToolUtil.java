package xdean.tool.api;

import static xdean.jex.util.task.TaskUtil.*;

import java.lang.reflect.Modifier;

import lombok.experimental.UtilityClass;
import rx.Observable;
import rx.functions.Func1;
import xdean.jex.util.string.StringUtil;
import xdean.tool.api.impl.TextToolItem;

@UtilityClass
public class ToolUtil {

  /**
   * Get tools from the class without any processing.
   * 
   * @param clz
   * @return
   */
  public Observable<ITool> getTool(Class<?> clz) {
    // has @Tool
    if (clz.getAnnotation(Tool.class) == null) {
      return Observable.empty();
    }
    return Observable.concat(
        Observable.just(clz)
            .filter(c -> ITool.class.isAssignableFrom(c))
            .map(c -> uncatch(() -> c.newInstance()))
            .filter(i -> i != null)
            .cast(ITool.class),
        // field
        Observable.from(clz.getDeclaredFields())
            .filter(f -> (f.getModifiers() & Modifier.PUBLIC & Modifier.STATIC & Modifier.FINAL) != 0)
            .filter(f -> f.getAnnotation(Tool.class) != null)
            .filter(f -> IToolGetter.class.isAssignableFrom(f.getType()))
            .map(f -> uncheck(() -> f.get(null)))
            .cast(IToolGetter.class)
            .map(IToolGetter::get),
        // method
        Observable.from(clz.getDeclaredMethods())
            .filter(m -> (m.getModifiers() & Modifier.PUBLIC & Modifier.STATIC) != 0)
            .filter(m -> m.getAnnotation(Tool.class) != null)
            .filter(m -> m.getParameterCount() == 0)
            .filter(m -> ITool.class.isAssignableFrom(m.getReturnType()))
            .map(m -> uncheck(() -> m.invoke(null, new Object[] {})))
            .cast(ITool.class));
  }

  public Observable<ITool> getWrappedTool(Class<?> clz) {
    return getTool(clz).map(ToolUtil::wrapTool);
  }

  public <T extends ITool> ITool wrapTool(ITool tool) {
    return wrapTool(tool, str -> new TextToolItem(getLastPath(str)));
  }

  /**
   * 
   * @param tool the tool to wrap
   * @param func from absolute path to tool
   * @return
   */
  public <T extends ITool> ITool wrapTool(ITool tool, Func1<String, ITool> func) {
    return Observable.just(tool.getClass())
        .map(ToolUtil::getToolPath)
        .flatMap(p -> Observable.from(p.split("/")))
        .filter(s -> s.length() > 0)
        .scan((s1, s2) -> String.join("/", s1, s2))
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

  public String getLastPath(String path) {
    return path.substring(path.lastIndexOf("/") + 1);
  }
}
