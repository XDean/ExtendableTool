package xdean.tool.tray.handler;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import lombok.extern.slf4j.Slf4j;
import xdean.jex.util.file.FileUtil;
import xdean.jex.util.task.TaskUtil;
import xdean.tool.api.ToolUtil;
import xdean.tool.api.inter.ITool;
import xdean.tool.api.inter.IToolResourceLoader;

@Slf4j
public class ToolJarLoader implements IToolResourceLoader {

  @Override
  public List<ITool> getTools(Path path) {
    try {
      ToolJarUrl toolJarUrl = new ToolJarUrl(path);
      return toolJarUrl.getToolMenu();
    } catch (IOException e) {
      log.error("Illegal url", path, e);
    }
    return Collections.emptyList();
  }

  private static class ToolJarUrl {

    URL url;
    Path libPath;
    JarURLConnection connection;

    public ToolJarUrl(Path path) throws IOException {
      this.url = path.toAbsolutePath().toUri().toURL();
      this.libPath = getLibURL(path);
      try {
        connection = (JarURLConnection) url.openConnection();
      } catch (Exception e) {
        url = new URL(String.format("jar:%s!/", url));
        connection = (JarURLConnection) url.openConnection();
      }
    }

    public List<ITool> getToolMenu() throws IOException {
      List<ITool> list = new ArrayList<>();
      URLClassLoader classLoader = new URLClassLoader(getURLs(), getClass().getClassLoader());
      JarFile jarFile = connection.getJarFile();
      ArrayList<JarEntry> jarEntries = Collections.list(jarFile.entries());
      for (JarEntry jarEntry : jarEntries) {
        String name = jarEntry.getName();
        if (name.endsWith(".jar")) {
          // classLoader.newInstance(urls)
        }
      }
      for (JarEntry jarEntry : jarEntries) {
        String name = jarEntry.getName();
        if (name.endsWith(".class")) {
          loadClass(name, classLoader).ifPresent(i -> list.add(i));
        }
      }
      return list;
    }

    /**
     * Default lib path with name "xxx_lib"
     * 
     * @param path
     * @return
     * @throws MalformedURLException
     */
    private Path getLibURL(Path path) throws MalformedURLException {
      return path.resolveSibling(FileUtil.getNameWithoutSuffix(path) + "_lib");
    }

    /**
     * Get urls with the jar file and its lib
     * 
     * @return
     */
    private URL[] getURLs() {
      List<URL> list = new ArrayList<>();
      list.add(url);
      FileUtil.wideTraversal(libPath)
          .filter(p -> p.getFileName().toString().endsWith(".jar"))
          .map(p -> TaskUtil.uncheck(() -> p.toUri().toURL()))
          .forEach(list::add);
      URL[] urls = new URL[list.size()];
      log.debug(list.toString());
      return list.toArray(urls);
    }

    private Optional<ITool> loadClass(String clzName, ClassLoader classLoader) {
      clzName = clzName.replace('/', '.');
      if (clzName.endsWith(".class")) {
        clzName = clzName.substring(0, clzName.length() - 6);
      }
      try {
        Class<?> clz = classLoader.loadClass(clzName);
        return ToolUtil.getToolMenu(clz);
      } catch (Throwable e) {
        log.error("load class fail", clzName, e);
      }
      return Optional.empty();
    }
  }
}
