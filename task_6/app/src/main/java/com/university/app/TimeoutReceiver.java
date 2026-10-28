package com.university.app;

import android.content.*;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TimeoutReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    PendingResult result = goAsync();
    ExecutorService pool = Executors.newSingleThreadExecutor();
    AtomicBoolean finished = new AtomicBoolean();
    Future<?> task =
        pool.submit(
            () -> {
              try {
                Thread.sleep(i.getLongExtra("delay", 3000));
                Log.i("A80_20", "async job finished");
              } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
              } finally {
                if (finished.compareAndSet(false, true)) result.finish();
              }
            });
    new Handler(Looper.getMainLooper())
        .postDelayed(
            () -> {
              if (finished.compareAndSet(false, true)) {
                Log.w("A80_20", "8s TIMEOUT");
                result.finish();
                task.cancel(true);
              }
              pool.shutdownNow();
            },
            8000);
  }
}
