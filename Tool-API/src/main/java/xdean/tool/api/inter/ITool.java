package xdean.tool.api.inter;

import java.net.URL;
import java.util.List;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

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

	void onClick();

}
