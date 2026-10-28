package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.util.Log;
import java.util.concurrent.*;

public final class DemoService extends Service {
  public static final String ACTION_COUNT = "com.university.app.COUNT",
      ACTION_MEDIA = "com.university.app.MEDIA",
      ACTION_SYNC = "com.university.app.SYNC";
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private volatile boolean stopped;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null) {
      Log.i("A80_STICKY", "intent null=true");
      return START_NOT_STICKY;
    }
    String action = intent.getAction();
    if (ACTION_COUNT.equals(action)) {
      executor.execute(
          () -> {
            for (int i = 60; i >= 0 && !stopped; i--) {
              Log.i("A80_1_1", "seconds=" + i);
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
              }
            }
            Log.i("A80_1_3", "stopSelf(" + startId + ")");
            stopSelf(startId);
          });
      return START_NOT_STICKY;
    }
    boolean media = ACTION_MEDIA.equals(action);
    String channel = media ? "media" : "sync";
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_LOW));
    Notification n =
        new Notification.Builder(this, channel)
            .setContentTitle(media ? "Аудиоплеер: demo" : "Синхронизация")
            .setContentText("Android80")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build();
    if (Build.VERSION.SDK_INT >= 29)
      startForeground(
          media ? 101 : 102,
          n,
          media
              ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
              : ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
    else startForeground(media ? 101 : 102, n);
    if (!media)
      executor.execute(
          () -> {
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            Log.i("A80_SYNC", "done");
            stopSelf(startId);
          });
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    stopped = true;
    executor.shutdownNow();
    Log.i("A80_1_3", "onDestroy");
    super.onDestroy();
  }
}
