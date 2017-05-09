package xdean.tool.sys;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.AbstractToolItem;

@Tool
public class ShutdownTool extends AbstractToolItem {

	public ShutdownTool() {
		super("Shutdown");
		childrenProperty().add(new TimedShutdown());
	}

	@Override
	public void onClick() {

	}

	private static class TimedShutdown extends AbstractToolItem {
		private static final String DO = "Timed";
		private static final String CANCEL = "Cancel";
		BooleanProperty timed = new SimpleBooleanProperty(false);

		public TimedShutdown() {
			super(DO);
			timed.addListener((ob, o, n) -> textProperty().set(n ? CANCEL : DO));
		}

		@Override
		public void onClick() {
			if (timed.get()) {
				try {
					int result = Runtime.getRuntime().exec("cmd /c shutdown -a").waitFor();
					if (result == 0) {
						Util.showMessage("Cancel success.");
						timed.set(false);
					} else {
						Util.showMessage("Cancel fail");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Util.showInputInteger("Input shutdown delay in second")
						.ifPresent(i -> {
							try {
								int result = Runtime.getRuntime().exec("cmd /c shutdown -s -t " + i).waitFor();
								if (result == 0) {
									timed.set(true);
								}
							} catch (InterruptedException | IOException e) {
								e.printStackTrace();
							}
						});
			}
		}
	}
}
