package com.university.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.File;
import java.util.concurrent.*;

public final class CacheCleanupService extends Service {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @Override
  public int onStartCommand(Intent i, int f, int id) {
    executor.execute(
        () -> {
          long old = System.currentTimeMillis() - 7L * 86400000L;
          long freed = clean(getCacheDir(), old);
          Log.i("A80_8", "freed_bytes=" + freed);
          stopSelf(id);
        });
    return START_NOT_STICKY;
  }

  private static long clean(File dir, long before) {
    long freed = 0;
    File[] all = dir.listFiles();
    if (all == null) return 0;
    for (File f : all) {
      if (f.isDirectory()) {
        freed += clean(f, before);
        File[] children = f.listFiles();
        if (children != null && children.length == 0) f.delete();
      } else if (f.lastModified() < before) {
        long len = f.length();
        if (f.delete()) freed += len;
      }
    }
    return freed;
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    executor.shutdownNow();
    super.onDestroy();
  }
}
