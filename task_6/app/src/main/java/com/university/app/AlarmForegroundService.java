package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;

public final class AlarmForegroundService extends Service {
  private final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public void onCreate() {
    super.onCreate();
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel("alarm", "Будильник", NotificationManager.IMPORTANCE_HIGH));
  }

  @Override
  public int onStartCommand(Intent i, int f, int id) {
    Intent screen =
        new Intent(this, AlarmScreenActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent full =
        PendingIntent.getActivity(
            this, 0, screen, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    Notification n =
        new Notification.Builder(this, "alarm")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Будильник Android80")
            .setContentText("Открыть экран будильника")
            .setCategory(Notification.CATEGORY_ALARM)
            .setFullScreenIntent(full, true)
            .build();
    if (Build.VERSION.SDK_INT >= 34)
      startForeground(233, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    else startForeground(233, n);
    handler.removeCallbacksAndMessages(null);
    handler.postDelayed(() -> stopSelf(id), 30000);
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacksAndMessages(null);
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }
}
