package xdean.tool.sys.other;

import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.io.InputStreamReader;

import com.google.common.io.CharStreams;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import xdean.jex.util.log.Logable;
import xdean.tool.api.impl.AbstractToolItem;

public class CommandTool extends AbstractToolItem implements Logable {

  public static CommandTool create(String text, String... command) {
    CommandTool tool = new CommandTool(command);
    tool.textProperty().set(text);
    return tool;
  }

  ProcessBuilder pb;
  String command;

  protected CommandTool(String... command) {
    pb = new ProcessBuilder(command);
    this.command = String.join(" ", command);
  }

  @Override
  public void onClick() {
    Process exec = uncheck(pb::start);
    Observable.merge(
        Observable.fromCallable(() -> CharStreams.toString(new InputStreamReader(exec.getInputStream())))
            .doOnNext(s -> log().info("[{}] output: {}", command, s)),
        Observable.fromCallable(() -> CharStreams.toString(new InputStreamReader(exec.getErrorStream())))
            .doOnNext(s -> log().error("[{}] error: {}", command, s)))
        .subscribeOn(Schedulers.newThread())
        .doOnComplete(() -> log().info("[{}] completed.", command))
        .subscribe();
  }
}
