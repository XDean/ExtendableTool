package xdean.tool.sys.other;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import xdean.tool.api.impl.AbstractToolItem;

@AllArgsConstructor
public class CommandTool extends AbstractToolItem {

  public static CommandTool create(String text, String command) {
    CommandTool tool = new CommandTool(command);
    tool.textProperty().set(text);
    return tool;
  }

  String command;

  @SneakyThrows
  @Override
  public void onClick() {
    Runtime.getRuntime().exec(command);
  }

}
