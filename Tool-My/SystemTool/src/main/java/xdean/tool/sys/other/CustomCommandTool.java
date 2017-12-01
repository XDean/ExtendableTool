package xdean.tool.sys.other;

import static xdean.jex.util.lang.ExceptionUtil.uncheck;
import static xdean.jex.util.log.LogUtil.log;
import static xdean.jex.util.string.StringUtil.*;
import static xdean.jex.util.task.TaskUtil.async;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import xdean.jex.util.task.If;
import xdean.tool.api.Config;
import xdean.tool.api.Context;
import xdean.tool.api.ITool;
import xdean.tool.api.Tool;
import xdean.tool.api.impl.TextToolItem;
import xdean.tool.sys.SystemTools;

/**
 * Define custom command in file.<br>
 * The setting file is located "Context.HOME_PATH/Custom Command Tool Setting.properties".<br>
 * Format by "name = command".
 *
 * @author XDean
 *
 */
@Tool(parent = SystemTools.class, path = "Custom")
public class CustomCommandTool extends TextToolItem{

  private static final Path CUSTOM_PATH = Paths.get(Context.HOME_PATH, Config.getProperty("Custom Command Tool",
      "Custom Command Tool Setting.properties"));

  private boolean loaded = false;
  private List<ITool> customs = new LinkedList<>();

  public CustomCommandTool() {
    super("Loading...");
    parentProperty().addListener((ob, o, n) -> {
      if (loaded && n != null) {
        removeFromParent();
        customs.forEach(n::addChild);
      }
    });
    async(this::load);
  }

  private void load() {
    if (Files.notExists(CUSTOM_PATH)) {
      uncheck(() -> Files.createFile(CUSTOM_PATH));
    }
    uncheck(() -> Files.readAllLines(CUSTOM_PATH)).stream()
        .map(s -> s.split("=", 2))
        .filter(s -> If.that(s.length == 2)
            .ordo(() -> log().warn("Custom command should be NAME=COMMAND. {} is not right.", s[0]))
            .condition())
        .forEach(s -> add(s[0].trim(), "cmd /c " + s[1].trim()));
    removeFromParent();
    loaded = true;
  }

  private void add(String name, String command) {
    CommandTool custom = CommandTool.create(name, split(command));
    customs.add(custom);
    if (getParent() != null) {
      getParent().addChild(custom);
    }
  }

  static String[] split(String command) {
    Iterator<Character> placeHolder = notExistChars(command);
    if (placeHolder.hasNext() == false) {
      throw new IllegalArgumentException("Can't handle the command");
    }
    log().debug("init is : " + command);
    // replace \\
    String backslashPh = String.valueOf(placeHolder.next());
    command = command.replace("\\\\", backslashPh);
    log().debug("after \\\\: " + command);
    // replace \"
    String quotePh = String.valueOf(placeHolder.next());
    command = command.replace("\\\"", quotePh);
    log().debug("after \\\": " + command);
    // replace content with quote
    String inQuotePh = String.valueOf(placeHolder.next());
    Queue<String> inQuotes = new LinkedList<>();
    int[] indexes;
    while ((indexes = getInquote(command))[1] != -1) {
      inQuotes.add(command.substring(indexes[0], indexes[1]));
      command = replacePart(command, indexes[0], indexes[1], inQuotePh);
    }
    log().debug("after \"\": " + command);
    return Stream.of(command.split(" "))
        .map(s -> {
          int index;
          while ((index = s.indexOf(inQuotePh)) != -1) {
            s = replacePart(s, index, index + 1, inQuotes.poll());
          }
          return s;
        })
        .map(s -> s.replace(quotePh, "\\\""))
        .map(s -> s.replace(backslashPh, "\\\\"))
        .toArray(i -> new String[i]);
  }

  private static int[] getInquote(String string) {
    int i1 = string.indexOf('"');
    int i2 = i1 == -1 ? -1 : string.indexOf('"', i1 + 1);
    return new int[] { i1, i2 == -1 ? -1 : i2 + 1 };
  }
}
