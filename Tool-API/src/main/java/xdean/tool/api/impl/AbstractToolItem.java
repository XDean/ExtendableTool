package xdean.tool.api.impl;

import java.net.URL;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import xdean.tool.api.ITool;

public abstract class AbstractToolItem implements ITool {

  protected StringProperty text;
  protected ObjectProperty<URL> iconUrl;
  protected ListProperty<ITool> children;

  public AbstractToolItem() {
    text = new SimpleStringProperty("undefined");
    iconUrl = new SimpleObjectProperty<>();
    children = new SimpleListProperty<>(FXCollections.observableArrayList());
  }

  public AbstractToolItem(String text) {
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
