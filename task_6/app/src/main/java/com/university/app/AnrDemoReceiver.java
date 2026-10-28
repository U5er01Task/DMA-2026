package com.university.app;

import android.content.*;
import android.util.Log;

public final class AnrDemoReceiver extends BroadcastReceiver {
  public void onReceive(Context c, Intent i) {
    Log.i("A80_4_2", "onReceive 15000ms BEGIN");
    try {
      Thread.sleep(15000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    Log.i("A80_4_2", "onReceive END");
  }
}
