package xdean.tool.tray.handler;

import static xdean.jex.util.task.TaskUtil.uncheck;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;

import lombok.extern.slf4j.Slf4j;
import xdean.jex.util.file.FileUtil;
import xdean.tool.api.Context;
import xdean.tool.api.ITool;
import xdean.tool.api.IToolResourceLoader;
import xdean.tool.api.ToolUtil;

@Slf4j
public class ToolJarLoader implements IToolResourceLoader {

  @Override
  public List<ITool> getTools(Path path) {
    try {
      return new ToolJarUrl(path).getToolMenu();
    } catch (IOException e) {
      log.error("Illegal url", path, e);
    }
    return Collections.emptyList();
  }

  private static class ToolJarUrl {
    URL sourceUrl;
    Path sourcePath;
    Path libPath;
    ArrayList<JarEntry> jarEntries;

    public ToolJarUrl(Path path) throws IOException {
      this.sourcePath = path;
      this.sourceUrl = path.toAbsolutePath().toUri().toURL();
      this.libPath = getLibURL(path);
      System.out.println(sourceUrl);
      JarURLConnection connection;
      try {
        connection = (JarURLConnection) sourceUrl.openConnection();
      } catch (Exception e) {
        sourceUrl = new URL(String.format("jar:%s!/", sourceUrl));
        connection = (JarURLConnection) sourceUrl.openConnection();
      }
      jarEntries = Collections.list(connection.getJarFile().entries());
    }

    public List<ITool> getToolMenu() throws IOException {
      List<ITool> toolList = new ArrayList<>();
      URLClassLoader classLoader = getClassLoader();
      jarEntries.stream()
          .map(JarEntry::getName)
          .filter(n -> n.endsWith(".class"))
          .map(name -> loadClass(name, classLoader))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(toolList::add);
      return toolList;
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
     * Get urls with the jar file and its lib(inner and outer)
     * 
     * @return
     * @throws IOException
     */
    private URLClassLoader getClassLoader() throws IOException {
      List<URL> list = new ArrayList<>();
      // itself
      list.add(sourceUrl);
      // outer lib
      FileUtil.wideTraversal(libPath)
          .filter(p -> p.getFileName().toString().endsWith(".jar"))
          .map(p -> uncheck(() -> p.toUri().toURL()))
          .forEach(list::add);
      // inner lib
      URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { sourceUrl });
      Path tempLibFolder = Context.TEMP_PATH.resolve(sourcePath.getFileName().toString());
      FileUtil.createDirectory(tempLibFolder);
      // copy jar files from jar file to temp folder
      jarEntries.stream()
          .map(JarEntry::getName)
          .filter(n -> n.endsWith(".jar"))
          .forEach(name -> uncheck(() -> {
            InputStream input = classLoader.getResourceAsStream(name);
            Path tempPath = tempLibFolder.resolve(name);
            Files.copy(input, tempPath, StandardCopyOption.REPLACE_EXISTING);
            list.add(tempPath.toUri().toURL());
          }));
      classLoader.close();
      log.debug(list.toString());
      URL[] arr = new URL[list.size()];
      URL[] urls = list.toArray(arr);
      return URLClassLoader.newInstance(urls, getClass().getClassLoader());
    }

    private Optional<ITool> loadClass(String clzName, ClassLoader classLoader) {
      System.err.println("load"+clzName);
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
