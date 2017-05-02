package xdean.tool.api.impl;

import java.net.URL;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import xdean.tool.api.inter.ITool;

public enum SeparatorItem implements ITool {
	INSTANCE;
	@Override
	public StringProperty textProperty() {
		return null;
	}

	@Override
	public ObjectProperty<URL> iconUrlProperty() {
		return null;
	}

	@Override
	public ListProperty<ITool> childrenProperty() {
		return null;
	}

	@Override
	public void onClick() {
	}

}
