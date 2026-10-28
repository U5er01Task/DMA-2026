package com.university.receivers.low;

import android.content.*;
import android.util.Log;

public final class OrderedFinalReceiver extends BroadcastReceiver {
  public void onReceive(Context c, Intent i) {
    String prev = getResultData();
    String result = (prev == null ? "" : prev + "->") + "low";
    setResultData(result);
    Log.i("A80_19", "priority 100 result=" + result);
  }
}
