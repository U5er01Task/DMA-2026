package com.university.app;

import android.content.*;
import android.os.*;
import android.util.Log;

public final class UiThreadReceiver extends BroadcastReceiver {
  public void onReceive(Context c, Intent i) {
    Log.i("A80_4_1", "UI thread=" + (Looper.myLooper() == Looper.getMainLooper()));
  }
}
