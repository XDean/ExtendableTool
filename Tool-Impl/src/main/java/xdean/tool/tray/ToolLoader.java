package xdean.tool.tray;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import xdean.tool.api.inter.ITool;
import xdean.tool.api.inter.IToolResourceLoader;
import xdean.tool.tray.handler.ToolJarLoader;

public enum ToolLoader implements IToolResourceLoader {
	
	INSTANCE;
	
	ToolJarLoader jarHandler = new ToolJarLoader();
//	ToolClassLoader clzHandler = new ToolClassLoader();

	@Override
	public List<ITool> getTools(Path path) {
		String fileName = path.getFileName().toString();
		if (fileName.endsWith(".jar")) {
			return jarHandler.getTools(path);
		} 
//		else if (fileName.endsWith(".class")) {
//			return clzHandler.getTools(path);
//		}
		return Collections.emptyList();
	}

}
