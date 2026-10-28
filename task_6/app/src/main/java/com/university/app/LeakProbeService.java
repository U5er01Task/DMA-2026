package com.university.app;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import java.lang.ref.WeakReference;

public final class LeakProbeService extends Service {
  // статическое поле держит Activity дольше её жизни: классическая утечка
  static Activity held;
  static WeakReference<Activity> probe;
  private final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public int onStartCommand(Intent i, int flags, int id) {
    handler.postDelayed(this::check, 3000);
    return START_NOT_STICKY;
  }

  private void check() {
    System.gc();
    Activity a = probe == null ? null : probe.get();
    Log.i(
        "A80_44",
        "Activity уничтожена, но жива в памяти: "
            + (a != null && a.isDestroyed())
            + " (держит static LeakProbeService.held=" + (held != null) + ")");
    held = null;
    System.gc();
    Log.i(
        "A80_44",
        "после held=null Activity собрана GC: " + (probe == null || probe.get() == null));
    stopSelf();
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacksAndMessages(null);
    held = null;
    super.onDestroy();
  }
}
