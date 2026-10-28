package com.university.app;

import android.content.*;
import android.util.Log;

public final class ReplayReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    String nonce = i.getStringExtra("nonce");
    long time = i.getLongExtra("time", 0);
    boolean fresh = Math.abs(System.currentTimeMillis() - time) < 30000;
    boolean unused = c.getSharedPreferences("nonces", 0).getBoolean(nonce, false) == false;
    if (fresh && unused) {
      c.getSharedPreferences("nonces", 0).edit().putBoolean(nonce, true).apply();
      Log.i("A80_41", "accepted");
    } else Log.i("A80_41", "replay rejected");
  }
}
