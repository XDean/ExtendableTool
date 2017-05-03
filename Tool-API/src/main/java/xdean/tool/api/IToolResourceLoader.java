package xdean.tool.api;

import java.nio.file.Path;
import java.util.List;

public interface IToolResourceLoader {
  List<ITool> getTools(Path path);
}
