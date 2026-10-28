package com.university.app;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.util.Log;
import java.util.concurrent.*;

public final class CacheFetchService extends Service {
  private final ExecutorService io = Executors.newSingleThreadExecutor();

  @Override
  public int onStartCommand(Intent i, int flags, int id) {
    io.execute(
        () -> {
          try {
            Thread.sleep(500);
            ContentValues v = new ContentValues();
            v.put("title", "Downloaded cache item");
            v.put("category", "proxy");
            getContentResolver().insert(NotesProvider.URI, v);
            getContentResolver().notifyChange(CacheProxyProvider.URI, null);
            Log.i("A80_33", "background fetch complete");
          } catch (Exception e) {
            Log.e("A80_33", "fetch failed", e);
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
