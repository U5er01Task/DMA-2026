package com.university.app;

import android.content.*;
import android.util.Log;
import androidx.work.*;

public final class EventReceiver extends BroadcastReceiver {
  public void onReceive(Context c, Intent i) {
    String action = i.getAction();
    if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)
        || "com.university.app.TEST_STORAGE_LOW".equals(action)) {
      Log.w("A80_14", "Storage low -> remove temporary images");
      WorkManager.getInstance(c)
          .enqueue(new OneTimeWorkRequest.Builder(StorageCleanupWorker.class).build());
    }
  }
}
