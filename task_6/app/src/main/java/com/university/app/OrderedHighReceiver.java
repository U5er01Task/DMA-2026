package com.university.app;

import android.content.*;
import android.util.Log;

public final class OrderedHighReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    Log.i("A80_3_5", "priority 200");
    setResultData("high");
    if (i.getBooleanExtra("abort", false)) {
      Log.i("A80_3_5", "ordered broadcast aborted at 200");
      abortBroadcast();
    }
  }
}
