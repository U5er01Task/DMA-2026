package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.util.Log;
import java.util.concurrent.*;
import okhttp3.*;

public final class WebSocketForegroundService extends Service {
  public static final String EXTRA_URL = "url";
  private final OkHttpClient client =
      new OkHttpClient.Builder().pingInterval(25, TimeUnit.SECONDS).build();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private WebSocket socket;
  private String endpoint;
  private boolean stopped;
  private int retry = 0;

  @Override
  public void onCreate() {
    super.onCreate();
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel("sockets", "Socket", NotificationManager.IMPORTANCE_LOW));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int id) {
    endpoint = intent == null ? null : intent.getStringExtra(EXTRA_URL);
    if (endpoint == null || !(endpoint.startsWith("wss://") || endpoint.startsWith("ws://"))) {
      Log.e("A80_4", "Pass a WebSocket endpoint via EXTRA_URL");
      stopSelf(id);
      return START_NOT_STICKY;
    }
    Notification n =
        new Notification.Builder(this, "sockets")
            .setContentTitle("WebSocket")
            .setContentText("Подключение к серверу")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build();
    if (Build.VERSION.SDK_INT >= 29)
      startForeground(232, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
    else startForeground(232, n);
    connect();
    return START_STICKY;
  }

  private void connect() {
    if (stopped) return;
    socket =
        client.newWebSocket(
            new Request.Builder().url(endpoint).build(),
            new WebSocketListener() {
              @Override
              public void onOpen(WebSocket w, Response r) {
                retry = 0;
                Log.i("A80_4", "connected");
              }

              @Override
              public void onMessage(WebSocket w, String message) {
                Log.i("A80_4", "quote=" + message);
              }

              @Override
              public void onFailure(WebSocket w, Throwable e, Response response) {
                Log.w("A80_4", "socket failed", e);
                reconnect();
              }

              @Override
              public void onClosed(WebSocket w, int code, String reason) {
                Log.i("A80_4", "closed=" + code);
                reconnect();
              }
            });
  }

  private void reconnect() {
    if (stopped) return;
    long delay = Math.min(60000, 1000L * (1L << Math.min(retry++, 6)));
    handler.postDelayed(this::connect, delay);
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    stopped = true;
    handler.removeCallbacksAndMessages(null);
    if (socket != null) socket.close(1000, "stop");
    client.dispatcher().executorService().shutdown();
    super.onDestroy();
  }
}
