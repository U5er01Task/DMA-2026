package com.university.app;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;

public final class DownloaderService extends Service {
  @Override
  public int onStartCommand(Intent i, int f, int id) {
    Log.i(
        "A80_1_4",
        "pid="
            + android.os.Process.myPid()
            + " process="
            + (android.os.Build.VERSION.SDK_INT >= 28
                ? Application.getProcessName()
                : "downloader"));
    stopSelf(id);
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }
}
