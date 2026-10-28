package com.university.app;

import android.app.Activity;
import android.content.*;
import android.util.Log;

public final class SmsSentReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    Log.i(
        "A80_13",
        "SMS_SENT result="
            + getResultCode()
            + " success="
            + (getResultCode() == Activity.RESULT_OK));
  }
}
