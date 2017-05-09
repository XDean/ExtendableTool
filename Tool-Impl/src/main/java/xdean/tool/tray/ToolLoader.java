package xdean.tool.tray;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import xdean.tool.api.ITool;
import xdean.tool.api.IToolResourceLoader;
import xdean.tool.tray.loader.ToolJarLoader;

public enum ToolLoader implements IToolResourceLoader {

  INSTANCE;

  ToolJarLoader jarHandler = new ToolJarLoader();

  @Override
  public List<ITool> getTools(Path path) {
    String fileName = path.getFileName().toString();
    if (fileName.endsWith(".jar")) {
      return jarHandler.getTools(path);
    }
    return Collections.emptyList();
  }

}
