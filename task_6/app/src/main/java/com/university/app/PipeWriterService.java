package com.university.app;

import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public final class PipeWriterService extends Service {
  private final ExecutorService io = Executors.newSingleThreadExecutor();

  @Override
  public int onStartCommand(Intent i, int flags, int id) {
    ParcelFileDescriptor fd = i == null ? null : i.getParcelableExtra("output");
    if (fd == null) {
      stopSelf(id);
      return START_NOT_STICKY;
    }
    io.execute(
        () -> {
          try (OutputStream stream = new ParcelFileDescriptor.AutoCloseOutputStream(fd)) {
            stream.write(
                "Streaming Service -> ContentProvider via FD".getBytes(StandardCharsets.UTF_8));
            Log.i("A80_38", "pipe writer completed");
          } catch (IOException e) {
            Log.e("A80_38", "pipe writer", e);
          } finally {
            stopSelf(id);
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
    io.shutdownNow();
    super.onDestroy();
  }
}
