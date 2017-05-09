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
import lombok.extern.slf4j.Slf4j;
import xdean.tool.api.Context;
import xdean.tool.api.ITool;
import xdean.tool.api.impl.SeparatorItem;

@Slf4j
public enum TrayService {

  INSTANCE;

  TrayIcon tray;
  SystemTray systemTray;
  PopupMenu menuRoot;
  Map<ITool, MenuItem> menuMap;

  private TrayService() {
    systemTray = SystemTray.getSystemTray();
    menuMap = new HashMap<>();
  }

  public void start() throws AWTException {
    tray = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Context.HOME_PATH + "/icon.png"));
    menuRoot = new PopupMenu();
    MenuItem mi = new MenuItem();
    mi.setLabel("Exit");
    mi.addActionListener(e -> System.exit(0));
    menuRoot.addSeparator();
    menuRoot.add(mi);
    tray.setPopupMenu(menuRoot);
    systemTray.add(tray);
  }

  public void stop() {
    if (tray != null) {
      systemTray.remove(tray);
      tray = null;
      menuRoot = null;
    }
  }

  public void add(ITool tool) {
    menuRoot.insert(toMenuItem(tool), menuRoot.getItemCount() - 2);
  }

  public void addAll(List<ITool> tools) {
    tools.forEach(this::add);
  }

  public void remove(ITool tool) {
    menuRoot.remove(toMenuItem(tool));
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
                log.debug(remitem + "," + menuMap);
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
