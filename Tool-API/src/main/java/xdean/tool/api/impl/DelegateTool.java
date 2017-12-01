package xdean.tool.api.impl;

import java.net.URL;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import xdean.tool.api.ITool;

public class DelegateTool implements ITool {
  ITool delegate;

  public DelegateTool(ITool delegate) {
    this.delegate = delegate;
  }

  @Override
  public StringProperty textProperty() {
    return delegate.textProperty();
  }

  @Override
  public ObjectProperty<URL> iconUrlProperty() {
    return delegate.iconUrlProperty();
  }

  @Override
  public ListProperty<ITool> childrenProperty() {
    return delegate.childrenProperty();
  }

  @Override
  public ObjectProperty<ITool> parentProperty() {
    return delegate.parentProperty();
  }

  @Override
  public void onClick() {
    delegate.onClick();
  }

}
