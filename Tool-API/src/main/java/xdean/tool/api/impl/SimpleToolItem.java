package xdean.tool.api.impl;

public class SimpleToolItem extends AbstractToolItem {

  private final Runnable click;

  public SimpleToolItem(String text, Runnable click) {
    super(text);
    this.click = click;
  }

  @Override
  public void onClick() {
    click.run();
  }

}
