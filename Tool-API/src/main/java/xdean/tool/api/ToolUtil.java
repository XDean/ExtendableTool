package xdean.tool.api;

import java.util.Optional;

import xdean.tool.api.inter.ITool;
import xdean.tool.api.inter.ToolMenu;

public class ToolUtil {

	public static Optional<ITool> getToolMenu(Class<?> clz) {
		if (clz.getAnnotation(ToolMenu.class) == null) {
			return Optional.empty();
		}
		Object newInstance;
		try {
			newInstance = clz.newInstance();
		} catch (Exception e) {
			return Optional.empty();
		}
		if (newInstance instanceof ITool) {
			return Optional.of((ITool) newInstance);
		} else {
			return Optional.empty();
		}
	}

}
