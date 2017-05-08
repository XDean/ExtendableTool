package xdean.tool.sys;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import xdean.tool.api.impl.AbstractToolItem;

@AllArgsConstructor
public class CommandTool extends AbstractToolItem {

  String text;
  String command;

  @SneakyThrows
  @Override
  public void onClick() {
    Runtime.getRuntime().exec(command);
  }

}
