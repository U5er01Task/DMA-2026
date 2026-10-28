package com.university.app;

import android.app.Application;
import android.util.Log;

public final class DemoApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Log.i("A80_6_1", "Application.onCreate time=" + System.nanoTime());
  }
}
