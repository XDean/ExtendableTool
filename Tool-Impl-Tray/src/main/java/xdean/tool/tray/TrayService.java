package xdean.tool.tray;

import java.awt.AWTException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;
import sun.awt.AWTAccessor;
import xdean.jex.extra.Wrapper;
import xdean.jex.util.collection.MapUtil;
import xdean.tool.api.ITool;
import xdean.tool.api.ToolUtil;
import xdean.tool.api.impl.SeparatorItem;
import xdean.tool.api.impl.TextToolItem;

public enum TrayService {

  INSTANCE;

  TrayIcon tray;
  SystemTray systemTray;
  PopupMenu rootMenu;
  ITool rootTool;
  Map<ITool, MenuItem> menuMap;
  Map<String, ITool> pathMap;

  private TrayService() {
    systemTray = SystemTray.getSystemTray();
    menuMap = new HashMap<>();
    pathMap = new HashMap<>();
  }

  public void start() throws AWTException {
    tray = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Context.ICON_PATH));
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
    if (tool instanceof SeparatorItem) {
      return new MenuItem("-");
    }
    MenuItem item = menuMap.get(tool);
    if (item != null) {
      return item;
    }
    item = convertToMenuItem(tool);
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
