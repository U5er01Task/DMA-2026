package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.MediaPlayer;
import android.os.*;
import android.util.Log;

public final class MediaPlayerService extends Service {
  public static final String PLAY = "com.university.app.player.PLAY",
      PAUSE = "com.university.app.player.PAUSE",
      NEXT = "com.university.app.player.NEXT";
  static volatile boolean running;
  private MediaPlayer player;
  private boolean foreground;
  private int index;
  private final IBinder binder = new LocalBinder();

  public final class LocalBinder extends Binder {
    public MediaPlayerService getService() {
      return MediaPlayerService.this;
    }
  }

  public String getTrackName() {
    return "demo tone " + (index + 1);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    running = true;
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel("player", "Плеер", NotificationManager.IMPORTANCE_LOW));
    player = MediaPlayer.create(this, R.raw.demo_tone);
    player.setLooping(true);
  }

  private PendingIntent action(String name, int id) {
    Intent i = new Intent(this, MediaPlayerService.class).setAction(name);
    return PendingIntent.getService(
        this, id, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private void notifyPlayer() {
    Notification.Builder b =
        new Notification.Builder(this, "player")
            .setContentTitle("Android80 — трек " + (index + 1))
            .setContentText(player.isPlaying() ? "Играет" : "Пауза")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_play, "Play", action(PLAY, 1))
            .addAction(android.R.drawable.ic_media_pause, "Pause", action(PAUSE, 2))
            .addAction(android.R.drawable.ic_media_next, "Next", action(NEXT, 3));
    Notification n = b.build();
    if (!foreground) {
      if (Build.VERSION.SDK_INT >= 29)
        startForeground(240, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
      else startForeground(240, n);
      foreground = true;
    } else getSystemService(NotificationManager.class).notify(240, n);
  }

  @Override
  public int onStartCommand(Intent i, int flags, int id) {
    String a = i == null ? PLAY : i.getAction();
    if (PAUSE.equals(a)) player.pause();
    else if (NEXT.equals(a)) {
      index++;
      player.seekTo(0);
      player.start();
    } else player.start();
    Log.i("A80_2", "action=" + a + " track=" + index);
    notifyPlayer();
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent i) {
    return binder;
  }

  @Override
  public void onDestroy() {
    running = false;
    player.release();
    super.onDestroy();
  }
}
