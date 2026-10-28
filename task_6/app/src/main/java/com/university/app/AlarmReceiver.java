package com.university.app;

import android.content.*;
import android.os.Build;

public final class AlarmReceiver extends BroadcastReceiver {
  public void onReceive(Context c, Intent i) {
    Intent service = new Intent(c, AlarmForegroundService.class);
    if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(service);
    else c.startService(service);
  }
}
