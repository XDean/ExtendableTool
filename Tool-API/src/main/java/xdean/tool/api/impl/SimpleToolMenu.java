package xdean.tool.api.impl;


public class SimpleToolMenu extends AbstractToolMenu {

	private final Runnable click;

	public SimpleToolMenu(String text, Runnable click) {
		super(text);
		this.click = click;
	}

	@Override
	public void onClick() {
		click.run();
	}

}
