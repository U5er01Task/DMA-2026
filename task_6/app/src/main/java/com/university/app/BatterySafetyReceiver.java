package com.university.app;

import android.content.*;
import android.util.Log;

public final class BatterySafetyReceiver extends BroadcastReceiver {
  public void onReceive(Context c, Intent i) {
    if (Intent.ACTION_BATTERY_LOW.equals(i.getAction())
        || "com.university.app.TEST_LOW_BATTERY".equals(i.getAction())) {
      c.stopService(new Intent(c, EnterpriseSyncService.class));
      c.stopService(new Intent(c, DownloadForegroundService.class));
      c.getSharedPreferences("status", 0).edit().putBoolean("power_save", true).apply();
      Log.w("A80_32", "heavy services stopped; provider power_save=true");
    }
  }
}
