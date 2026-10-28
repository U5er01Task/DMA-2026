package com.university.app;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CallLogService extends Service {
  private final ExecutorService io = Executors.newSingleThreadExecutor();
  private ContentObserver observer;

  @Override
  public int onStartCommand(Intent i, int flags, int id) {
    if (observer == null) {
      observer =
          new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean self) {
              io.execute(CallLogService.this::countCalls);
            }
          };
      getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer);
      io.execute(this::countCalls);
      Log.i("A80_29", "call log observer enabled");
    }
    return START_NOT_STICKY;
  }

  private void countCalls() {
    int incoming = 0, outgoing = 0, missed = 0;
    long seconds = 0;
    try (Cursor c =
        getContentResolver()
            .query(
                CallLog.Calls.CONTENT_URI,
                new String[] {CallLog.Calls.TYPE, CallLog.Calls.DURATION},
                null,
                null,
                null)) {
      while (c != null && c.moveToNext()) {
        switch (c.getInt(0)) {
          case CallLog.Calls.INCOMING_TYPE:
            incoming++;
            break;
          case CallLog.Calls.OUTGOING_TYPE:
            outgoing++;
            break;
          case CallLog.Calls.MISSED_TYPE:
            missed++;
            break;
          default:
            break;
        }
        seconds += c.getLong(1);
      }
      Log.i(
          "A80_29",
          "статистика: входящих=" + incoming + " исходящих=" + outgoing + " пропущенных=" + missed
              + " всего секунд разговора=" + seconds);
    } catch (SecurityException e) {
      Log.e("A80_29", "нет разрешения READ_CALL_LOG", e);
    }
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    if (observer != null) getContentResolver().unregisterContentObserver(observer);
    io.shutdownNow();
    super.onDestroy();
  }
}
