package xdean.tool.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Resource loader determine how to load tools from file
 * 
 * @author XDean
 *
 */
public interface IToolResourceLoader {
  List<ITool> getTools(Path path);
}
