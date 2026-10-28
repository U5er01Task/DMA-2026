package com.university.app;

import android.content.*;
import android.util.Log;

public final class ToggleReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    Log.i("A80_40", "toggle receiver active");
  }
}
