package xdean.tool.tray;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TrayServiceFx extends Application {

  public static final TrayServiceFx INSTANCE = new TrayServiceFx();

  public static void main(String[] args) throws AWTException {
    launch(args);
  }

  SystemTray  systemTray;
  TrayIcon    tray;
  Stage       stage;
  ContextMenu rootMenu;

  @Override
  public void start(Stage primaryStage) throws Exception {
    Platform.setImplicitExit(false);
    systemTray = SystemTray.getSystemTray();
    rootMenu = createContextMenu();
    stage = primaryStage;
    stage = new Stage(StageStyle.UTILITY);
    stage.setScene(new Scene(new Group(), 0, 0, Color.TRANSPARENT));
    start();
    stage.setOnHidden(e->stage.hide());
    rootMenu.setOnAutoHide(e->System.out.println("stage hide"));
  }

  public void start() throws AWTException {
    tray = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Context.ICON_PATH));
    tray.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
          System.out.println("click");
          Platform.runLater(() -> show(e.getX(), e.getY()));
        }
      }
    });
    systemTray.add(tray);
  }

  @Override
  public void stop() {
    if (tray != null) {
      systemTray.remove(tray);
      tray = null;
    }
  }

  private void show(int x, int y) {
    System.out.println("show");
    stage.setX(Double.MAX_VALUE);
    stage.setY(Double.MAX_VALUE);
    stage.show();

    rootMenu.setX(x);
    rootMenu.setY(y);
    rootMenu.show(stage);
  }

  private ContextMenu createContextMenu() {
    MenuItem game = new MenuItem("Global Thermonuclear War");
    game.setOnAction(event -> {
      System.out.println("This is not a good option.");
      rootMenu.hide();
      stage.hide();
    });

    MenuItem exit = new MenuItem("Exit");

    return new ContextMenu(
        game,
        exit);
  }
}
