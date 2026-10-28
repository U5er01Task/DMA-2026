package com.university.app;

import android.content.*;
import android.util.Log;

public final class LocaleRefreshReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    if (Intent.ACTION_LOCALE_CHANGED.equals(i.getAction())) {
      c.getSharedPreferences("locale_cache", 0).edit().clear().apply();
      Log.i("A80_18", "localized cache reset, locale=" + java.util.Locale.getDefault());
    }
  }
}
