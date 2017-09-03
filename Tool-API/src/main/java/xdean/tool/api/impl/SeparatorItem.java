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

public enum SeparatorItem implements ITool {
  INSTANCE;
  protected StringProperty text;
  protected ObjectProperty<URL> iconUrl;
  protected ObjectProperty<ITool> parent;
  protected ListProperty<ITool> children;

  private SeparatorItem() {
    text = new SimpleStringProperty("SeparatorItem");
    iconUrl = new SimpleObjectProperty<>();
    parent = new SimpleObjectProperty<>();
    children = new SimpleListProperty<>(FXCollections.observableArrayList());
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

  @Override
  public ObjectProperty<ITool> parentProperty() {
    return parent;
  }

  @Override
  public void onClick() {
  }

}
