package xdean.tool.tray.loader;

import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;

import io.reactivex.Observable;
import xdean.jex.util.file.FileUtil;
import xdean.jex.util.log.Logable;
import xdean.tool.api.Context;
import xdean.tool.api.ITool;
import xdean.tool.api.IToolResourceLoader;
import xdean.tool.api.ToolUtil;

/**
 * ToolJarLoader will load .jar file.<br>
 * It resolve both packaged library in jar file and library in a sub-folder named "XXX_lib".
 *
 * @author XDean
 *
 */
public class ToolJarLoader implements IToolResourceLoader, Logable {
  private static class InnerClassLoader extends URLClassLoader {
    public InnerClassLoader(ClassLoader parent) {
      super(new URL[] {}, parent);
    }

    @Override
    public void addURL(URL url) {
      super.addURL(url);
    }
  }

  private InnerClassLoader libClassLoader = new InnerClassLoader(getClass().getClassLoader());
  private Path tempLibFolder = Context.TEMP_PATH.resolve("classpath");

  @Override
  public List<ITool> getTools(Path path) {
    try {
      return new ToolJarUrl(path).getTools();
    } catch (IOException e) {
      log().error("Illegal url", path, e);
    }
    return Collections.emptyList();
  }

  private class ToolJarUrl implements Logable {
    private final Path libPath;
    private final ArrayList<JarEntry> jarEntries;
    private final URLClassLoader toolClassLoader;

    public ToolJarUrl(Path path) throws IOException {
      URL sourceUrl = path.toAbsolutePath().toUri().toURL();
      JarURLConnection connection;
      try {
        connection = (JarURLConnection) sourceUrl.openConnection();
      } catch (Exception e) {
        connection = (JarURLConnection) new URL(String.format("jar:%s!/", sourceUrl)).openConnection();
      }
      this.libPath = getLibURL(path);
      this.jarEntries = Collections.list(connection.getJarFile().entries());
      this.toolClassLoader = URLClassLoader.newInstance(new URL[] { sourceUrl }, libClassLoader);
      loadLibraries();
    }

    public List<ITool> getTools() throws IOException {
      List<ITool> toolList = new ArrayList<>();
      Observable.fromIterable(jarEntries)
          .map(JarEntry::getName)
          .filter(n -> n.endsWith(".class"))
          .flatMap(name -> loadClass(name))
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
    private void loadLibraries() throws IOException {
      // outer lib
      FileUtil.wideTraversal(libPath)
          .filter(p -> p.getFileName().toString().endsWith(".jar"))
          .map(p -> uncheck(() -> p.toUri().toURL()))
          .forEach(libClassLoader::addURL);
      // inner lib
      Files.createDirectories(tempLibFolder);

      // copy jar files from jar file to temp folder
      jarEntries.stream()
          .map(JarEntry::getName)
          .filter(n -> n.endsWith(".jar"))
          .forEach(name -> uncheck(() -> {
            InputStream input = toolClassLoader.getResourceAsStream(name);
            Path targetPath = tempLibFolder.resolve(Paths.get(name).getFileName());
            URL targetURL = targetPath.toUri().toURL();
            try {
              Files.copy(input, targetPath);
            } catch (FileAlreadyExistsException e) {
            }
            libClassLoader.addURL(targetURL);
          }));
    }

    private Observable<ITool> loadClass(String clzName) {
      clzName = clzName.replace('/', '.');
      if (clzName.endsWith(".class")) {
        clzName = clzName.substring(0, clzName.length() - 6);
      }
      try {
        Class<?> clz = toolClassLoader.loadClass(clzName);
        return ToolUtil.loadTool(clz);
      } catch (Throwable e) {
        log().error("load class fail", clzName, e);
      }
      return Observable.empty();
    }
  }
}
