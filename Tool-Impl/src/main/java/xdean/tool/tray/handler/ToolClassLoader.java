package xdean.tool.tray.handler;

public class ToolClassLoader {

}
//
// import java.io.IOException;
// import java.net.MalformedURLException;
// import java.net.URL;
// import java.net.URLClassLoader;
// import java.nio.file.Path;
// import java.util.ArrayList;
// import java.util.List;
//
// import lombok.extern.slf4j.Slf4j;
// import xdean.tool.api.ToolUtil;
// import xdean.tool.api.inter.ITool;
// import xdean.tool.api.inter.IToolResourceLoader;
//
// @Slf4j
// public class ToolClassLoader implements IToolResourceLoader {
//
// @Override
// public List<ITool> getTools(Path path) {
// List<ITool> list = new ArrayList<>();
// try (URLClassLoader classLoader = new URLClassLoader(new URL[] {
// path.toAbsolutePath().toUri().toURL() },
// Thread.currentThread().getContextClassLoader())) {
// Class<?> clz = classLoader.loadClass(path.getFileName().toString());
// ToolUtil.getToolMenu(clz).ifPresent(list::add);
// } catch (ClassNotFoundException e) {
// log.error("Class not found", path, e);
// } catch (IOException e1) {
// log.error("Class loader init fail", path, e1);
// }
// return list;
// }
//
// }
