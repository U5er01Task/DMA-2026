package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.net.*;
import android.os.*;
import android.util.Log;
import java.util.concurrent.*;

public final class EnterpriseSyncService extends Service {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private ConnectivityManager connectivity;
  private BroadcastReceiver networkReceiver;
  private volatile boolean alive;

  @Override
  public void onCreate() {
    super.onCreate();
    connectivity = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel(
                  "enterprise", "Enterprise Sync", NotificationManager.IMPORTANCE_LOW));
  }

  @Override
  public int onStartCommand(Intent intent, int f, int id) {
    Notification n =
        new Notification.Builder(this, "enterprise")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Enterprise: dataSync")
            .setContentText("Синхронизация страниц")
            .setOngoing(true)
            .build();
    if (Build.VERSION.SDK_INT >= 29)
      startForeground(250, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
    else startForeground(250, n);
    alive = true;
    networkReceiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context c, Intent i) {
            Network active = connectivity.getActiveNetwork();
            NetworkCapabilities caps =
                active == null ? null : connectivity.getNetworkCapabilities(active);
            boolean online =
                caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            Log.i("A80_50", "network online=" + online);
          }
        };
    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    if (Build.VERSION.SDK_INT >= 33)
      registerReceiver(networkReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    else registerReceiver(networkReceiver, filter);
    executor.execute(
        () -> {
          try {
            int offset = 0;
            while (alive) {
              int count = 0;
              Uri uri =
                  NotesProvider.URI
                      .buildUpon()
                      .appendQueryParameter("limit", "20")
                      .appendQueryParameter("offset", String.valueOf(offset))
                      .build();
              try (Cursor c =
                  getContentResolver()
                      .query(uri, new String[] {"_id", "title"}, null, null, "_id")) {
                if (c != null) {
                  count = c.getCount();
                  Log.i("A80_50", "offset=" + offset + " count=" + count);
                }
              }
              if (count < 20) break;
              offset += count;
            }
          } catch (Exception e) {
            Log.e("A80_50", "query", e);
          } finally {
            stopSelf(id);
          }
        });
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    alive = false;
    if (networkReceiver != null)
      try {
        unregisterReceiver(networkReceiver);
      } catch (IllegalArgumentException ignored) {
      }
    executor.shutdownNow();
    super.onDestroy();
  }
}
