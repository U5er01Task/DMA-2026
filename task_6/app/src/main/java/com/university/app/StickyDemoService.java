package com.university.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;

public class StickyDemoService extends Service {
  Handler h = new Handler(Looper.getMainLooper());
  int sec;
  Runnable tick =
      new Runnable() {
        public void run() {
          Log.i("A80_2_2", "sec=" + (sec++));
          h.postDelayed(this, 1000);
        }
      };

  @Override
  public void onCreate() {
    super.onCreate();
    h.post(tick);
  }

  @Override
  public int onStartCommand(Intent i, int flags, int startId) {
    Log.i("A80_2_2", "intent null=" + (i == null));
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    h.removeCallbacks(tick);
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }
}
