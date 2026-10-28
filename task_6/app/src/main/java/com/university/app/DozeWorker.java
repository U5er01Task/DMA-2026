package com.university.app;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.*;

public final class DozeWorker extends Worker {
  public DozeWorker(@NonNull Context c, @NonNull WorkerParameters params) {
    super(c, params);
  }

  @NonNull
  @Override
  public Result doWork() {
    PowerManager pm =
        (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock lock =
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Android80:DozeDemo");
    try {
      lock.acquire(10000);
      Log.i(
          "A80_45",
          "powerSave="
              + pm.isPowerSaveMode()
              + " idle="
              + (android.os.Build.VERSION.SDK_INT >= 23 && pm.isDeviceIdleMode()));
      Thread.sleep(1000);
      return Result.success();
    } catch (Exception e) {
      Log.e("A80_45", "Work failed", e);
      return Result.failure();
    } finally {
      if (lock.isHeld()) lock.release();
      Log.i("A80_45", "WakeLock released");
    }
  }
}
