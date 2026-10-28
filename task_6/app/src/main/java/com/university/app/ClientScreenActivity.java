package com.university.app;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.widget.*;

public final class ClientScreenActivity extends Activity {
  private RandomService srv;
  private boolean bound;
  private TextView view;
  private final ServiceConnection conn =
      new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
          srv = ((RandomService.LocalBinder) binder).getService();
          srv.registerClient();
          view.setText("Client #2: " + srv.getRandomNumber());
        }

        public void onServiceDisconnected(ComponentName name) {
          srv = null;
        }
      };

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    view = new TextView(this);
    view.setTextSize(24);
    setContentView(view);
  }

  @Override
  protected void onStart() {
    super.onStart();
    bound = bindService(new Intent(this, RandomService.class), conn, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop() {
    if (bound) {
      if (srv != null) srv.unregisterClient();
      unbindService(conn);
      bound = false;
    }
    super.onStop();
  }
}
