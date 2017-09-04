package xdean.tool.api;

import java.net.URL;
import java.util.List;

import xdean.jex.util.task.If;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

/**
 * Tool item, it has 4 properties<br>
 * 1. text<br>
 * 2. icon<br>
 * 3. children<br>
 * 4. parent<br>
 * and an action {@code onClick}
 *
 * @author XDean
 *
 */
public interface ITool {

  StringProperty textProperty();

  default String getText() {
    return textProperty().get();
  }

  ObjectProperty<URL> iconUrlProperty();

  default URL getIconUrl() {
    return iconUrlProperty().get();
  }

  ListProperty<ITool> childrenProperty();

  default List<ITool> getChildren() {
    ListProperty<ITool> children = childrenProperty();
    return children.subList(0, children.size());
  }

  default boolean addChild(ITool tool) {
    return If.that(tool.removeFromParent())
        .and(() -> this.childrenProperty().add(tool))
        .todo(() -> tool.parentProperty().set(this))
        .condition();
  }

  default boolean removeChild(ITool tool) {
    return If.that(childrenProperty().remove(tool))
        .todo(() -> tool.parentProperty().set(null))
        .condition();
  }

  default boolean removeFromParent() {
    ITool parent = getParent();
    return parent == null || parent.removeChild(this);
  }

  ObjectProperty<ITool> parentProperty();

  default ITool getParent() {
    return parentProperty().get();
  }

  void onClick();

}
