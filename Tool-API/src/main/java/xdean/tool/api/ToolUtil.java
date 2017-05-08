package xdean.tool.api;

import java.util.Optional;

public class ToolUtil {

	public static Optional<ITool> getToolMenu(Class<?> clz) {
		if (clz.getAnnotation(Tool.class) == null) {
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
