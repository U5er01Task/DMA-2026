package com.university.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

public final class AlarmScreenActivity extends Activity {
  static final String ACTION_DISMISS = "com.university.app.DISMISS_ALARM";

  private final BroadcastReceiver dismiss =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
          finish();
        }
      };

  @Override
  public void onCreate(Bundle b) {
    super.onCreate(b);
    if (Build.VERSION.SDK_INT >= 27) {
      setShowWhenLocked(true);
      setTurnScreenOn(true);
    } else
      getWindow()
          .addFlags(
              WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                  | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    Button stop = new Button(this);
    stop.setText("Отключить будильник");
    stop.setOnClickListener(
        v -> {
          stopService(new Intent(this, AlarmForegroundService.class));
          finish();
        });
    setContentView(stop);
  }

  @Override
  protected void onStart() {
    super.onStart();
    IntentFilter filter = new IntentFilter(ACTION_DISMISS);
    if (Build.VERSION.SDK_INT >= 33)
      registerReceiver(dismiss, filter, Context.RECEIVER_NOT_EXPORTED);
    else registerReceiver(dismiss, filter);
  }

  @Override
  protected void onStop() {
    unregisterReceiver(dismiss);
    super.onStop();
  }
}
