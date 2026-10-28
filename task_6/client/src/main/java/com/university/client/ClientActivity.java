package com.university.client;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

public final class ClientActivity extends Activity {
  public void onCreate(Bundle b) {
    super.onCreate(b);
    LinearLayout box = new LinearLayout(this);
    box.setOrientation(1);
    Button button = new Button(this);
    button.setText("Читать корзину другого приложения");
    TextView label = new TextView(this);
    box.addView(button);
    box.addView(label);
    Button whitelist = new Button(this);
    whitelist.setText("Проверить ACL Messenger");
    box.addView(whitelist);
    setContentView(box);
    whitelist.setOnClickListener(
        v -> {
          Intent target = new Intent();
          target.setComponent(
              new ComponentName(
                  "com.university.app", "com.university.app.WhitelistedMessengerService"));
          ServiceConnection conn =
              new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder b) {
                  Messenger svc = new Messenger(b);
                  Messenger reply =
                      new Messenger(
                          new Handler(
                              Looper.getMainLooper(),
                              m -> {
                                label.setText("ACL: " + m.arg1);
                                Log.i("A80_CLIENT", "permission=" + m.arg1);
                                return true;
                              }));
                  Message request = Message.obtain(null, 1);
                  request.replyTo = reply;
                  try {
                    svc.send(request);
                  } catch (RemoteException e) {
                    label.setText(e.toString());
                  }
                }

                public void onServiceDisconnected(ComponentName name) {}
              };
          boolean bound = bindService(target, conn, Context.BIND_AUTO_CREATE);
          if (!bound) label.setText("ACL service unavailable");
        });
    button.setOnClickListener(
        v -> {
          StringBuilder sb = new StringBuilder();
          try (Cursor cursor =
              getContentResolver()
                  .query(
                      Uri.parse("content://com.university.app.notes/notes"),
                      new String[] {"_id", "title", "category"},
                      "category=?",
                      new String[] {"cart"},
                      "_id DESC")) {
            while (cursor != null && cursor.moveToNext())
              sb.append(cursor.getLong(0)).append(": ").append(cursor.getString(1)).append("\n");
          } catch (Exception error) {
            sb.append(error);
          }
          label.setText(sb.length() == 0 ? "Корзина пуста" : sb.toString());
        });
  }
}
