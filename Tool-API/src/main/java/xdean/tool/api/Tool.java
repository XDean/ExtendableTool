package xdean.tool.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class with {@code @Tool} will be loaded and <br>
 * 1. Construct an instance to tried to convert to a tool item<br>
 * 2. Each public static final {@code IToolGetter} with {@code @Tool} will be tried to call {@code get} and convert
 * to tool item.<br>
 * 3. Each public static method with {@code @Tool} with no parameter will be called and convert the result to toll item.
 * 
 * For situation 2 and 3, parent is the class by default.
 * 
 * @author XDean
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface Tool {

  Class<?> parent() default Object.class;

  /**
   * Relative path to parent, split by '/'
   */
  String path() default "";
}
