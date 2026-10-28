package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.util.Log;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public final class DownloadForegroundService extends Service {
  public static final String EXTRA_TEMPLATE = "template",
      ACTION_FINISHED = "com.university.app.DOWNLOAD_FINISHED",
      CHANNEL = "downloads";
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private volatile boolean stopped;

  @Override
  public void onCreate() {
    super.onCreate();
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel(CHANNEL, "100 файлов", NotificationManager.IMPORTANCE_LOW));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    String template = intent == null ? null : intent.getStringExtra(EXTRA_TEMPLATE);
    show(0);
    executor.execute(
        () -> {
          if (template == null || !template.startsWith("https://") || !template.contains("{n}")) {
            Log.e("A80_3", "HTTPS template must contain {n}; no files downloaded");
            stopSelf(startId);
            return;
          }
          File folder = new File(getExternalFilesDir(null), "download100");
          folder.mkdirs();
          int ok = 0;
          for (int i = 1; i <= 100 && !stopped; i++) {
            File file = new File(folder, String.format(java.util.Locale.US, "%03d.bin", i));
            if (file.isFile() && file.length() > 0) {
              ok++;
              show(i);
              continue;
            }
            File part = new File(folder, file.getName() + ".part");
            HttpURLConnection c = null;
            try {
              String u = template.replace("{n}", String.format(java.util.Locale.US, "%03d", i));
              c = (HttpURLConnection) new URL(u).openConnection();
              c.setConnectTimeout(10000);
              c.setReadTimeout(15000);
              c.setInstanceFollowRedirects(false);
              int status = c.getResponseCode();
              if (status != 200) throw new IOException("HTTP " + status);
              try (InputStream in = c.getInputStream();
                  FileOutputStream out = new FileOutputStream(part)) {
                byte[] data = new byte[8192];
                int len;
                while ((len = in.read(data)) != -1) {
                  if (stopped) break;
                  out.write(data, 0, len);
                }
              }
              if (stopped) break;
              if (!part.renameTo(file)) throw new IOException("rename failed");
              ok++;
              Log.i("A80_3", "downloaded=" + file.getName());
            } catch (Exception e) {
              Log.e("A80_3", "item=" + i, e);
              part.delete();
            } finally {
              if (c != null) c.disconnect();
            }
            show(i);
          }
          if (!stopped) {
            Intent done =
                new Intent(ACTION_FINISHED).setPackage(getPackageName()).putExtra("count", ok);
            sendBroadcast(done);
            Log.i("A80_31", "broadcast complete success=" + ok);
            stopSelf(startId);
          }
        });
    return START_REDELIVER_INTENT;
  }

  private void show(int value) {
    Notification n =
        new Notification.Builder(this, CHANNEL)
            .setContentTitle("Загрузка 100 файлов")
            .setContentText(value + "/100")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, value, false)
            .setOngoing(true)
            .build();
    if (value == 0) {
      if (Build.VERSION.SDK_INT >= 29)
        startForeground(109, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
      else startForeground(109, n);
    } else getSystemService(NotificationManager.class).notify(109, n);
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    stopped = true;
    executor.shutdownNow();
    super.onDestroy();
  }
}
