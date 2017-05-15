package xdean.tool.sys.other;

import java.nio.charset.Charset;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;
import xdean.tool.api.impl.AbstractToolItem;

@Slf4j
public class CommandTool extends AbstractToolItem {

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

  @SneakyThrows
  @Override
  public void onClick() {
    Process exec = pb.start();
    Observable.merge(
        StringObservable.from(exec.getInputStream())
            .compose(o -> StringObservable.decode(o, Charset.defaultCharset()))
            .single()
            .doOnNext(s -> log.info("[{}] output: {}", command, s)),
        StringObservable.from(exec.getErrorStream())
            .compose(o -> StringObservable.decode(o, Charset.defaultCharset()))
            .single()
            .doOnNext(s -> log.error("[{}] error: {}", command, s)))
        .subscribeOn(Schedulers.newThread())
        .doOnCompleted(() -> log.info("[{}] completed.", command))
        .subscribe();
  }
}
