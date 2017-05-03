package xdean.tool.sys;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import xdean.tool.api.impl.AbstractToolMenu;

@AllArgsConstructor
public class CommandTool extends AbstractToolMenu {

  String text;
  String command;

  @SneakyThrows
  @Override
  public void onClick() {
    Runtime.getRuntime().exec(command);
  }

}
