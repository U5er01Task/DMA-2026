package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.*;
import android.util.Log;

public final class ClipboardMonitorService extends Service {
  private ClipboardManager clipboard;
  private final ClipboardManager.OnPrimaryClipChangedListener listener = this::onClip;

  @Override
  public void onCreate() {
    super.onCreate();
    getSystemService(NotificationManager.class)
        .createNotificationChannel(
            new NotificationChannel("clip", "Буфер обмена", NotificationManager.IMPORTANCE_LOW));
    clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
  }

  @Override
  public int onStartCommand(Intent i, int flags, int id) {
    Notification n =
        new Notification.Builder(this, "clip")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Мониторинг буфера обмена")
            .setContentText("Ищу телефонные номера")
            .setOngoing(true)
            .build();
    if (Build.VERSION.SDK_INT >= 34)
      startForeground(242, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
    else startForeground(242, n);
    clipboard.removePrimaryClipChangedListener(listener);
    clipboard.addPrimaryClipChangedListener(listener);
    Log.i("A80_9", "clipboard listener on");
    return START_NOT_STICKY;
  }

  private void onClip() {
    // с Android 10 в фоне getPrimaryClip() возвращает null, пока приложение не на экране
    ClipData clip = clipboard.getPrimaryClip();
    if (clip == null || clip.getItemCount() == 0) {
      Log.i("A80_9", "clipboard changed, content hidden by Android (app in background)");
      return;
    }
    CharSequence text = clip.getItemAt(0).coerceToText(this);
    if (text == null || !text.toString().matches("\\+?[0-9() .-]{7,22}")) return;
    Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(text.toString())));
    PendingIntent call =
        PendingIntent.getActivity(this, 0, dial, PendingIntent.FLAG_IMMUTABLE);
    Notification n =
        new Notification.Builder(this, "clip")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Номер найден: " + text)
            .setContentText("Нажмите, чтобы позвонить")
            .setContentIntent(call)
            .addAction(android.R.drawable.ic_menu_call, "Позвонить", call)
            .setAutoCancel(true)
            .build();
    getSystemService(NotificationManager.class).notify(243, n);
    Log.i("A80_9", "phone number in clipboard: " + text);
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    clipboard.removePrimaryClipChangedListener(listener);
    getSystemService(NotificationManager.class).cancel(243);
    super.onDestroy();
  }
}
