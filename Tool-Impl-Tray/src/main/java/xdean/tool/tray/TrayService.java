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
    MenuItem item = menuMap.get(tool);
    if (item != null) {
      return item;
    }
    item = convertToMenuItem(tool);
    menuMap.put(tool, item);
    return item;
  }

  private MenuItem convertToMenuItem(ITool tool) {
    List<ITool> children = tool.childrenProperty();
    if (tool instanceof SeparatorItem) {
      return new MenuItem("-");
    }
    if (children.size() == 0) {
      MenuItem item = new MenuItem();
      item.setLabel(tool.getText());
      item.addActionListener(e -> tool.onClick());
      tool.textProperty().addListener((ob, o, n) -> item.setLabel(n));
      return item;
    } else {
      Menu menu = new Menu();
      menu.setLabel(tool.getText());
      tool.textProperty().addListener((ob, o, n) -> menu.setLabel(n));
      children.forEach(item -> menu.add(toMenuItem(item)));
      tool.childrenProperty().addListener(new ListChangeListener<ITool>() {
        @Override
        public void onChanged(Change<? extends ITool> c) {
          while (c.next()) {
            if (c.wasPermutated()) {
            } else if (c.wasUpdated()) {
            } else {
              for (ITool remitem : c.getRemoved()) {
                MenuItem removed = menuMap.remove(remitem);
                if (removed != null) {
                  menu.remove(removed);
                }
              }
              for (ITool additem : c.getAddedSubList()) {
                MenuItem item = toMenuItem(additem);
                menu.add(item);
              }
            }
          }
        }
      });
      return menu;
    }
  }
}
