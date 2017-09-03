package xdean.tool.api;

import java.net.URL;
import java.util.List;

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

  default void addChild(ITool tool) {
    tool.removeFromParent();
    this.childrenProperty().add(tool);
    tool.parentProperty().set(this);
  }

  default boolean removeChild(ITool tool) {
    if (childrenProperty().remove(tool)) {
      tool.parentProperty().set(null);
      return true;
    }
    return false;
  }

  default boolean removeFromParent() {
    ITool parent = parentProperty().get();
    return parent != null && parent.removeChild(this);
  }

  ObjectProperty<ITool> parentProperty();

  default ITool getParent() {
    return parentProperty().get();
  }

  void onClick();

}
