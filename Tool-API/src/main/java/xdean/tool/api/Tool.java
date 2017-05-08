package xdean.tool.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class with this annotation will be load as a tool item
 * 
 * @author XDean
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Tool {
  Class<?> parent() default Object.class;

  String path() default "";
}
