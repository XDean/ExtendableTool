package xdean.tool.api.impl;

import java.net.URL;

import xdean.tool.api.ITool;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public abstract class AbstractToolMenu implements ITool {

	protected StringProperty text;
	protected ObjectProperty<URL> iconUrl;
	protected ListProperty<ITool> children;

	public AbstractToolMenu() {
		text = new ReadOnlyStringWrapper("undefined");
		iconUrl = new ReadOnlyObjectWrapper<>();
		children = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
	}

	public AbstractToolMenu(String text) {
		this();
		this.text.set(text);
	}

	@Override
	public StringProperty textProperty() {
		return text;
	}

	@Override
	public ObjectProperty<URL> iconUrlProperty() {
		return iconUrl;
	}

	@Override
	public ListProperty<ITool> childrenProperty() {
		return children;
	}
}
