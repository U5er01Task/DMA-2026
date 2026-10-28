package com.university.app;

import android.app.Activity;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

// Задание 4.5: ресивер не снимается, в Logcat будет IntentReceiverLeaked
public final class LeakDemoActivity extends Activity {
  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    BroadcastReceiver leaked =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context c, Intent i) {}
        };
    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
    if (Build.VERSION.SDK_INT >= 33)
      registerReceiver(leaked, filter, Context.RECEIVER_NOT_EXPORTED);
    else registerReceiver(leaked, filter);
    Log.i("A80_4_5", "ресивер зарегистрирован, unregisterReceiver() не вызывается, закрываем Activity");
    finish();
  }
}
