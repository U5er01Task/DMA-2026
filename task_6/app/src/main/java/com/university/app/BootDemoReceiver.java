package com.university.app;

import android.content.*;
import android.util.Log;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public final class BootDemoReceiver extends BroadcastReceiver {
  static final String TEST_BOOT = "com.university.app.TEST_BOOT";
  static final String TEST_AUTOSTART = "com.university.app.TEST_BOOT_AUTOSTART";

  @Override
  public void onReceive(Context c, Intent i) {
    String action = i.getAction();
    boolean boot = Intent.ACTION_BOOT_COMPLETED.equals(action);
    if (!boot && !TEST_BOOT.equals(action) && !TEST_AUTOSTART.equals(action)) return;
    Log.i("A80_BOOT", "boot=" + action);
    c.sendBroadcast(
        new Intent(c, AsyncPreferenceReceiver.class).setAction("com.university.app.SAVE_EVENT"));
    WorkManager.getInstance(c).enqueue(new OneTimeWorkRequest.Builder(CleanupWorker.class).build());

    // цепочка из задания 15: BOOT_COMPLETED -> настройки -> startForegroundService()
    if (!boot && !TEST_AUTOSTART.equals(action)) return;
    boolean enabled = c.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("autostart", false);
    if (!enabled) {
      Log.i("A80_15", "автозапуск выключен в настройках, служба не стартует");
      return;
    }
    try {
      c.startForegroundService(new Intent(c, TimedForegroundService.class));
      Log.i("A80_15", "startForegroundService(TimedForegroundService)");
    } catch (RuntimeException e) {
      Log.w("A80_15", "запуск foreground-службы запрещён системой", e);
    }
  }
}
