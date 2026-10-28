package com.university.app;

import android.app.Service;
import android.content.*;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import java.util.concurrent.*;

public final class BackupService extends Service {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @Override
  public int onStartCommand(Intent i, int f, int id) {
    executor.execute(
        () -> {
          try (Cursor c = getContentResolver().query(NotesProvider.URI, null, null, null, null)) {
            JSONArrayCompat.save(this, c);
            Log.i("A80_36", "Backup triggered by tick");
          } catch (Exception e) {
            Log.e("A80_36", "backup failed", e);
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
    executor.shutdownNow();
    super.onDestroy();
  }
}
