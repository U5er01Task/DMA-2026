package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import androidx.media3.common.MediaItem;
import androidx.media3.effect.Presentation;
import androidx.media3.transformer.*;
import java.io.File;
import java.util.*;

public final class VideoTranscodeService extends Service {
  private static final String CHANNEL = "transcode";
  private Transformer transformer;
  private final java.util.ArrayDeque<Uri> queue = new java.util.ArrayDeque<>();
  private final java.util.concurrent.ExecutorService preparation =
      java.util.concurrent.Executors.newSingleThreadExecutor();
  private final Handler main = new Handler(Looper.getMainLooper());
  private boolean running;
  private boolean foreground;

  @Override
  public void onCreate() {
    super.onCreate();
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel(
                  CHANNEL, "Видео экспорт", NotificationManager.IMPORTANCE_LOW));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Uri input = intent == null ? null : intent.getData();
    if (input == null) {
      if (!running) stopSelf(startId);
      return START_NOT_STICKY;
    }
    Notification n =
        new Notification.Builder(this, CHANNEL)
            .setContentTitle("Сжатие видео")
            .setContentText("Экспорт 480p: очередь " + (queue.size() + 1))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build();
    if (!foreground) {
      if (Build.VERSION.SDK_INT >= 29)
        startForeground(245, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
      else startForeground(245, n);
      foreground = true;
    } else getSystemService(NotificationManager.class).notify(245, n);
    queue.add(input);
    startNext();
    return START_NOT_STICKY;
  }

  private void startNext() {
    if (running) return;
    Uri input = queue.poll();
    if (input == null) {
      stopSelf();
      return;
    }
    running = true;
    preparation.execute(
        () -> {
          try {
            android.content.res.AssetFileDescriptor descriptor =
                getContentResolver().openAssetFileDescriptor(input, "r");
            if (descriptor != null) {
              Log.i("A80_6", "input length=" + descriptor.getLength());
              descriptor.close();
            }
          } catch (Exception e) {
            Log.w("A80_6", "cannot preflight source", e);
          }
          main.post(() -> export(input));
        });
  }

  private void export(Uri input) {
    try {
      File output =
          new File(getExternalFilesDir(null), "transcoded_" + System.currentTimeMillis() + ".mp4");
      EditedMediaItem edited =
          new EditedMediaItem.Builder(MediaItem.fromUri(input))
              .setEffects(
                  new Effects(
                      Collections.emptyList(),
                      Collections.singletonList(Presentation.createForHeight(480))))
              .build();
      transformer =
          new Transformer.Builder(this)
              .addListener(
                  new Transformer.Listener() {
                    @Override
                    public void onCompleted(Composition c, ExportResult result) {
                      Log.i(
                          "A80_6",
                          "transcoded=" + output.getAbsolutePath() + " bytes=" + output.length());
                      finishItem();
                    }

                    @Override
                    public void onError(Composition c, ExportResult result, ExportException e) {
                      Log.e("A80_6", "transcoding failed", e);
                      finishItem();
                    }
                  })
              .build();
      transformer.start(edited, output.getAbsolutePath());
    } catch (Exception e) {
      Log.e("A80_6", "cannot start transcode", e);
      finishItem();
    }
  }

  private void finishItem() {
    running = false;
    transformer = null;
    startNext();
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    preparation.shutdownNow();
    main.removeCallbacksAndMessages(null);
    if (transformer != null) transformer.cancel();
    super.onDestroy();
  }
}
