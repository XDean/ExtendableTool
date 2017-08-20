package xdean.tool.api;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.experimental.UtilityClass;
import rx.Observable;
import rx.functions.Func1;
import xdean.jex.extra.Pair;
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
    Tool clzTool = clz.getAnnotation(Tool.class);
    if (clzTool == null) {
      return Observable.empty();
    }
    return Observable.concat(
        Observable.just(clz)
            .filter(c -> ITool.class.isAssignableFrom(c))
            .map(c -> uncatch(() -> c.newInstance()))
            .cast(ITool.class)
            .filter(t -> t != null),
        Observable.<Pair<ITool, Tool>> concat(
            // field
            Observable.from(clz.getDeclaredFields())
                .filter(f -> (~f.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) == 0)
                .flatMap(field ->
                    Observable.just(field)
                        .filter(f -> f.getAnnotation(Tool.class) != null)
                        .filter(f -> IToolGetter.class.isAssignableFrom(f.getType()))
                        .map(f -> uncatch(() -> f.get(null)))
                        .cast(IToolGetter.class)
                        .map(IToolGetter::get)
                        .flatMap(t ->
                            Observable.just(field)
                                .map(f -> f.getAnnotation(Tool.class))
                                .map(anno -> {
                                  if (anno.parent() == defaultTool().parent()) {
                                    return parent(anno, clz);
                                  } else {
                                    return anno;
                                  }
                                })
                                .map(anno -> Pair.of(t, anno)))
                ),
            // method
            Observable.from(clz.getDeclaredMethods())
                .filter(m -> ((~m.getModifiers()) & (Modifier.PUBLIC | Modifier.STATIC)) == 0)
                .flatMap(method ->
                    Observable.just(method)
                        .filter(m -> m.getAnnotation(Tool.class) != null)
                        .filter(m -> m.getParameterCount() == 0)
                        .filter(m -> ITool.class.isAssignableFrom(m.getReturnType()))
                        .map(m -> uncatch(() -> m.invoke(null, new Object[] {})))
                        .cast(ITool.class)
                        .flatMap(t ->
                            Observable.just(method)
                                .map(f -> f.getAnnotation(Tool.class))
                                .map(anno -> {
                                  if (anno.parent() == defaultTool().parent()) {
                                    return parent(anno, clz);
                                  } else {
                                    return anno;
                                  }
                                })
                                .map(anno -> Pair.of(t, anno)))
                )
            )
            .filter(p -> p.getLeft() != null && p.getRight() != null)
            .map(p -> new ToolWithAnno(p.getLeft(), p.getRight()))
        );
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
    return Observable.just(tool)
        .map(ToolUtil::getToolAnno)
        .map(ToolUtil::getToolPath)
        .flatMap(p -> Observable.from(p.split("/")))
        .filter(s -> s.length() > 0)
        .scan((s1, s2) -> String.join("/", s1, s2))
        // .startWith("/") // root path XXX what's for
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

  public Tool getToolAnno(ITool tool) {
    Tool anno;
    if (tool instanceof ToolWithAnno) {
      anno = ((ToolWithAnno) tool).getAnno();
    } else {
      anno = tool.getClass().getAnnotation(Tool.class);
    }
    return anno;
  }

  public String getToolPath(Class<?> clz) {
    Tool tool = clz.getAnnotation(Tool.class);
    if (tool == null) {
      return "";
    }
    return getToolPath(tool);
  }

  public String getToolPath(Tool tool) {
    return String.join("/", getToolPath(tool.parent()), StringUtil.unWrap(tool.path(), "/", ""));
  }

  public String getLastPath(String path) {
    return path.substring(path.lastIndexOf("/") + 1);
  }

  Tool create(String path, Class<?> parent) {
    return new Tool() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Tool.class;
      }

      @Override
      public String path() {
        return path;
      }

      @Override
      public Class<?> parent() {
        return parent;
      }
    };
  }

  Tool path(Tool origin, String path) {
    return create(path, origin.parent());
  }

  Tool parent(Tool origin, Class<?> parent) {
    return create(origin.path(), parent);
  }

  Tool defaultTool() {
    return DefaultTool.defaultTool;
  }

  @Tool
  @UtilityClass
  private class DefaultTool {
    Tool defaultTool = DefaultTool.class.getAnnotation(Tool.class);
  }

  @AllArgsConstructor
  private class ToolWithAnno implements ITool {
    @Delegate
    ITool itool;
    @Getter
    Tool anno;
  }
}
