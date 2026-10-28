package com.university.app;

import android.content.*;
import android.util.Log;

public final class AsyncPreferenceReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    final PendingResult pending = goAsync();
    new Thread(
            () -> {
              try {
                c.getSharedPreferences("settings", Context.MODE_PRIVATE)
                    .edit()
                    .putLong("last_boot", System.currentTimeMillis())
                    .commit();
                Log.i("A80_4_4", "saved last_boot");
              } finally {
                pending.finish();
              }
            })
        .start();
  }
}
