package com.university.app;

import android.content.*;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

// Действия: com.university.app.PLAY / PAUSE / STOP / MUTE / UNMUTE
public final class PlaybackControlReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    String action = i.getAction();
    if (action == null) return;
    Log.i("A80_34", "received=" + action);
    switch (action) {
      case "com.university.app.PLAY":
        startPlayer(c, MediaPlayerService.PLAY);
        break;
      case "com.university.app.PAUSE":
        if (MediaPlayerService.running) startPlayer(c, MediaPlayerService.PAUSE);
        break;
      case "com.university.app.STOP":
        c.stopService(new Intent(c, MediaPlayerService.class));
        break;
      case "com.university.app.MUTE":
        audio(c).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        break;
      case "com.university.app.UNMUTE":
        audio(c).adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        break;
      default:
        break;
    }
  }

  private static AudioManager audio(Context c) {
    return (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
  }

  private static void startPlayer(Context c, String action) {
    Intent player = new Intent(c, MediaPlayerService.class).setAction(action);
    try {
      if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(player);
      else c.startService(player);
    } catch (RuntimeException e) {
      Log.w("A80_34", "background FGS restricted", e);
    }
  }
}
