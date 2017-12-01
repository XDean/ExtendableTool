package xdean.tool.api;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import xdean.jex.extra.Pair;
import xdean.jex.util.string.StringUtil;
import xdean.jex.util.task.If;
import xdean.tool.api.impl.DelegateTool;
import xdean.tool.api.impl.TextToolItem;

public class ToolUtil {

  /**
   * Load tools from the class.
   *
   * @param clz
   * @return
   */
  public static Observable<ITool> loadTool(Class<?> clz) {
    // has @Tool
    Tool clzTool = clz.getAnnotation(Tool.class);
    if (clzTool == null) {
      return Observable.empty();
    }
    return Observable.concat(
        // class
        Observable.just(clz)
            .filter(c -> ITool.class.isAssignableFrom(c))
            .map(c -> uncatch(() -> c.newInstance()))
            .cast(ITool.class)
            .filter(t -> t != null)
            .map(t -> If.<ITool> that(clz.getDeclaringClass() != null)
                .and(() -> clzTool.parent() == defaultTool().parent())
                .tobe(new ToolWithAnno(t, parent(clzTool, clz.getDeclaringClass())))
                .orbe(t)
                .result()),
        Observable.<Pair<ITool, Tool>> concat(
            // field
            Observable.fromArray(clz.getDeclaredFields())
                .filter(f -> (~f.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) == 0)
                .flatMap(field -> Observable.just(field)
                    .filter(f -> f.getAnnotation(Tool.class) != null)
                    .filter(f -> IToolGetter.class.isAssignableFrom(f.getType()))
                    .map(f -> uncatch(() -> f.get(null)))
                    .cast(IToolGetter.class)
                    .map(IToolGetter::get)
                    .flatMap(t -> Observable.just(field)
                        .map(f -> f.getAnnotation(Tool.class))
                        .compose(defaultParent(t, clz)))),
            // method
            Observable.fromArray(clz.getDeclaredMethods())
                .filter(m -> ((~m.getModifiers()) & (Modifier.PUBLIC | Modifier.STATIC)) == 0)
                .flatMap(method -> Observable.just(method)
                    .filter(m -> m.getAnnotation(Tool.class) != null)
                    .filter(m -> m.getParameterCount() == 0)
                    .filter(m -> ITool.class.isAssignableFrom(m.getReturnType()))
                    .map(m -> uncatch(() -> m.invoke(null, new Object[] {})))
                    .cast(ITool.class)
                    .flatMap(t -> Observable.just(method)
                        .map(f -> f.getAnnotation(Tool.class))
                        .compose(defaultParent(t, clz)))))
            .filter(p -> p.getLeft() != null && p.getRight() != null)
            .map(p -> new ToolWithAnno(p.getLeft(), p.getRight())));
  }

  private static ObservableTransformer<Tool, Pair<ITool, Tool>> defaultParent(ITool tool, Class<?> loadingClass) {
    return o -> o.map(anno -> If.<Tool> that(anno.parent() == defaultTool().parent())
        .tobe(parent(anno, loadingClass))
        .orbe(anno)
        .result())
        .<Pair<ITool, Tool>> map(anno -> Pair.of(tool, anno));
  }

  public static Observable<ITool> getWrappedTool(Class<?> clz) {
    return loadTool(clz).map(ToolUtil::wrapTool);
  }

  public static <T extends ITool> ITool wrapTool(ITool tool) {
    return wrapTool(tool, str -> new TextToolItem(getLastPath(str)));
  }

  /**
   *
   * @param tool the tool to wrap
   * @param func from absolute path to tool
   * @return
   */
  public static <T extends ITool> ITool wrapTool(ITool tool, Function<String, ITool> func) {
    return Observable.just(tool)
        .map(ToolUtil::getToolAnno)
        .map(ToolUtil::getToolPath)
        .flatMap(p -> Observable.fromArray(p.split("/")))
        .filter(s -> s.length() > 0)
        .scan((s1, s2) -> String.join("/", s1, s2))
        .map(func::apply)
        .concatWith(Observable.just(tool))
        .scan((a, b) -> {
          a.addChild(b);
          return b;
        })
        .toList()
        .map(l -> l.get(0))
        .blockingGet();
  }

  public static Tool getToolAnno(ITool tool) {
    Tool anno;
    if (tool instanceof ToolWithAnno) {
      anno = ((ToolWithAnno) tool).getAnno();
    } else {
      anno = tool.getClass().getAnnotation(Tool.class);
    }
    return anno;
  }

  public static String getToolPath(Class<?> clz) {
    Tool tool = clz.getAnnotation(Tool.class);
    if (tool == null) {
      return "";
    }
    return getToolPath(tool);
  }

  public static String getToolPath(Tool tool) {
    return String.join("/", getToolPath(tool.parent()), StringUtil.unWrap(tool.path(), "/", ""));
  }

  public static String getLastPath(String path) {
    return path.substring(path.lastIndexOf("/") + 1);
  }

  private static Tool create(String path, Class<?> parent) {
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

  @SuppressWarnings("unused")
  private static Tool path(Tool origin, String path) {
    return create(path, origin.parent());
  }

  private static Tool parent(Tool origin, Class<?> parent) {
    return create(origin.path(), parent);
  }

  private static Tool defaultTool() {
    return DefaultTool.defaultTool;
  }

  @Tool
  private static class DefaultTool {
    static Tool defaultTool = DefaultTool.class.getAnnotation(Tool.class);
  }

  private static class ToolWithAnno extends DelegateTool {
    Tool anno;

    public ToolWithAnno(ITool tool, Tool anno) {
      super(tool);
      this.anno = anno;
    }

    public Tool getAnno() {
      return anno;
    }
  }
}
