package xdean.tool.tray;

import java.awt.EventQueue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

public class Util {

  public static Scheduler awt() {
    return AwtScheduler.INSTANCE;
  }

  private static final class AwtScheduler extends Scheduler {
    private static final Scheduler INSTANCE = new AwtScheduler();

    @Override
    public Worker createWorker() {
      return new Worker() {

        CompositeDisposable cd = new CompositeDisposable();

        @Override
        public boolean isDisposed() {
          return cd.isDisposed();
        }

        @Override
        public void dispose() {
          cd.dispose();
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
          Disposable d = Disposables.empty();
          if (delay <= 0) {
            schedule(run, d);
          } else {
            Single.timer(delay, unit, Schedulers.io()).subscribe(e -> schedule(run, d));
          }
          return Disposables.fromAction(() -> {
            d.dispose();
            cd.remove(d);
          });
        }

        private void schedule(Runnable run, Disposable d) {
          EventQueue.invokeLater(() -> {
            if (!(d.isDisposed() || cd.isDisposed())) {
              run.run();
            }
            cd.remove(d);
          });
        }
      };
    }
  }
}
