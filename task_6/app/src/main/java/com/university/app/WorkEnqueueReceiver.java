package com.university.app;

import android.content.*;
import android.util.Log;
import androidx.work.*;

public final class WorkEnqueueReceiver extends BroadcastReceiver {
  static final String ACTION = "com.university.app.ENQUEUE_WORK";
  static final String TAG = "A80_Worker";

  @Override
  public void onReceive(Context c, Intent i) {
    OneTimeWorkRequest request =
        new OneTimeWorkRequest.Builder(CleanupWorker.class).addTag(TAG).build();
    WorkManager.getInstance(c).enqueue(request);
    Log.i("A80_4_3", "onReceive: Worker поставлен в очередь, id=" + request.getId());
  }
}
