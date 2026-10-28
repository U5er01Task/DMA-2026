package com.university.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.*;
import java.util.concurrent.*;

public final class SingleFileTransferService extends Service {
  private final ExecutorService worker = Executors.newSingleThreadExecutor();

  @Override
  public int onStartCommand(Intent i, int flags, int startId) {
    worker.execute(
        () -> {
          File dest = new File(getCacheDir(), "transferred.bin");
          try (ByteArrayInputStream from = new ByteArrayInputStream(new byte[32000]);
              FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[4096];
            int len;
            int sent = 0;
            while ((len = from.read(buffer)) != -1) {
              out.write(buffer, 0, len);
              sent += len;
              Log.i("A80_1_3", "bytes=" + sent);
            }
          } catch (IOException e) {
            Log.e("A80_1_3", "copy", e);
          } finally {
            Log.i("A80_1_3", "stopSelf(" + startId + ")");
            stopSelf(startId);
          }
        });
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    worker.shutdownNow();
    Log.i("A80_1_3", "onDestroy");
    super.onDestroy();
  }
}
