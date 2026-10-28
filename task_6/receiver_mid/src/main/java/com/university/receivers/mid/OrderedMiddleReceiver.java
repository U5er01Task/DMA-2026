package com.university.receivers.mid;

import android.content.*;
import android.util.Log;

public final class OrderedMiddleReceiver extends BroadcastReceiver {
  public void onReceive(Context c, Intent i) {
    String prev = getResultData();
    String result = (prev == null ? "" : prev + "->") + "mid";
    setResultData(result);
    Log.i("A80_19", "priority 150 result=" + result);
  }
}
