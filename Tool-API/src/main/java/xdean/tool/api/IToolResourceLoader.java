package xdean.tool.api;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Resource loader determine how to load tools from file
 * 
 * @author XDean
 *
 */
public interface IToolResourceLoader {
  Collection<ITool> getTools(Path path);
}
