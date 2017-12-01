package xdean.tool.tray;

import static xdean.jex.util.lang.ExceptionUtil.*;

import java.awt.AWTException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import sun.awt.AWTAccessor;
import xdean.jex.extra.Wrapper;
import xdean.jex.util.collection.MapUtil;
import xdean.jex.util.log.Logable;
import xdean.tool.api.ITool;
import xdean.tool.api.ToolUtil;
import xdean.tool.api.impl.SeparatorItem;
import xdean.tool.api.impl.TextToolItem;

public enum TrayService implements Logable {

  INSTANCE;

  private static final String TITLE = "Tools";

  TrayIcon tray;
  SystemTray systemTray;
  PopupMenu rootMenu;
  ITool rootTool;
  Map<ITool, MenuItem> menuMap;
  Map<String, ITool> pathMap;
  StringProperty tooltip;

  private TrayService() {
    systemTray = SystemTray.getSystemTray();
    menuMap = new HashMap<>();
    pathMap = new HashMap<>();
    tooltip = new SimpleStringProperty();
  }

  private void initIcon() {
    Path DEFAULT_ICON = uncatch(() -> Paths.get(getClass().getResource("/default_icon.png").toURI()));
    Path path = Paths.get(Context.ICON_PATH);
    if (DEFAULT_ICON == null) {
      log().error("Default icon not found.");
      return;
    }
    if (Files.notExists(path)) {
      if (Files.exists(DEFAULT_ICON)) {
        uncheck(() -> Files.copy(DEFAULT_ICON, path));
      }
    }
  }

  public void start() throws AWTException {
    if (tray != null) {
      throw new Error("Can't start twice!");
    }
    initIcon();
    tray = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Context.ICON_PATH));
    JavaFxObservable.changesOf(tooltip)
        .observeOn(Util.awt())
        .forEach(c -> tray.setToolTip(c.getNewVal()));
    setToolTip(TITLE);
    rootTool = new TextToolItem("");
    rootMenu = new PopupMenu();
    menuMap.put(rootTool, rootMenu);
    pathMap.put("/", rootTool);
    MenuItem mi = new MenuItem();
    mi.setLabel("Exit");
    mi.addActionListener(e -> System.exit(0));
    rootMenu.addSeparator();
    rootMenu.add(mi);
    tray.setPopupMenu(rootMenu);
    systemTray.add(tray);
  }

  public void stop() {
    if (tray != null) {
      systemTray.remove(tray);
      tray = null;
      rootMenu = null;
    }
  }

  public void setToolTip(String text) {
    tooltip.set(text);
  }

  public void load(Path dir) throws IOException {
    setToolTip("loading...");
    if (Files.exists(dir) && Files.isDirectory(dir)) {
      Files.newDirectoryStream(dir).forEach(path -> {
        if (!Files.isDirectory(path)) {
          TrayService.INSTANCE.addAll(ToolLoader.INSTANCE.getTools(path));
        }
      });
    }
    setToolTip(TITLE);
  }

  public void add(ITool tool) {
    tool = ToolUtil.wrapTool(tool, path -> MapUtil.getOrPutDefault(pathMap, path,
        () -> new TextToolItem(ToolUtil.getLastPath(path))));
    rootMenu.insert(toMenuItem(tool), rootMenu.getItemCount() - 2);
  }

  public void addAll(List<ITool> tools) {
    tools.forEach(this::add);
  }

  public void remove(ITool tool) {
    rootMenu.remove(toMenuItem(tool));
  }

  private MenuItem toMenuItem(ITool tool) {
    MenuItem item = menuMap.get(tool);
    if (item != null) {
      return item;
    }
    item = tool instanceof SeparatorItem ? new MenuItem("-") : convertToMenuItem(tool);
    menuMap.put(tool, item);
    return item;
  }

  private int getIndex(Menu parent, MenuItem item) {
    return AWTAccessor.getMenuAccessor().getItems(parent).indexOf(item);
  }

  private MenuItem convertToMenuItem(ITool tool) {
    List<ITool> children = tool.childrenProperty();
    MenuItem menuItem = new MenuItem();
    menuItem.setLabel(tool.getText());
    menuItem.addActionListener(e -> tool.onClick());
    tool.textProperty().addListener((ob, o, n) -> menuItem.setLabel(n));

    Menu menu = new Menu();
    menu.setLabel(tool.getText());
    tool.textProperty().addListener((ob, o, n) -> menu.setLabel(n));
    children.forEach(item -> menu.add(toMenuItem(item)));

    Wrapper<Boolean> isItem = Wrapper.of(children.size() == 0);
    tool.childrenProperty().addListener((ListChangeListener<ITool>) c -> {
      while (c.next()) {
        // Item to menu
        if (c.getList().size() != 0 && isItem.get() == true) {
          Menu parent = (Menu) menuItem.getParent();
          if (parent != null) {
            int index = getIndex(parent, menuItem);
            parent.remove(index);
            parent.insert(menu, index);
            isItem.set(false);
          }
        }
        // Menu to item
        else if (c.getList().size() == 0 && isItem.get() == false) {
          Menu parent = (Menu) menu.getParent();
          if (parent != null) {
            int index = getIndex(parent, menu);
            parent.remove(index);
            parent.insert(menuItem, index);
            isItem.set(true);
          }
        }
        if (c.wasPermutated()) {
        } else if (c.wasUpdated()) {
        } else if (c.wasRemoved()) {
          for (ITool remitem : c.getRemoved()) {
            MenuItem removed = menuMap.remove(remitem);
            if (removed != null) {
              menu.remove(removed);
            }
          }
        } else if (c.wasAdded()) {
          for (ITool additem : c.getAddedSubList()) {
            MenuItem item = toMenuItem(additem);
            menu.add(item);
          }
        }
      }
    });

    if (children.size() == 0) {
      return menuItem;
    } else {
      return menu;
    }
  }
}
