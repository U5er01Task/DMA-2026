package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.util.Log;

public final class TimedForegroundService extends Service {
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Runnable stop =
      () -> {
        Log.i("A80_10", "30 minutes elapsed, stopping");
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
      };

  @Override
  public void onCreate() {
    super.onCreate();
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel("timer", "Таймер", NotificationManager.IMPORTANCE_LOW));
  }

  @Override
  public int onStartCommand(Intent i, int flags, int id) {
    Notification n =
        new Notification.Builder(this, "timer")
            .setContentTitle("Таймер автоостановки")
            .setContentText("Остановка через 30 минут")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build();
    if (Build.VERSION.SDK_INT >= 34)
      startForeground(241, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    else startForeground(241, n);
    handler.removeCallbacks(stop);
    handler.postDelayed(stop, 30 * 60 * 1000L);
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacks(stop);
    super.onDestroy();
  }
}
