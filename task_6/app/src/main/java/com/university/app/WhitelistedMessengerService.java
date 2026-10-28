package com.university.app;

import android.app.Service;
import android.content.*;
import android.os.*;
import android.util.Log;

public final class WhitelistedMessengerService extends Service {
  private final Messenger messenger =
      new Messenger(
          new Handler(
              Looper.getMainLooper(),
              msg -> {
                int uid = msg.sendingUid;
                String name = getPackageManager().getNameForUid(uid);
                boolean allow =
                    "com.university.app".equals(name) || "com.university.client".equals(name);
                Log.i("A80_37", "caller UID=" + uid + " name=" + name + " allow=" + allow);
                Message reply = Message.obtain(null, allow ? 1 : -1);
                reply.arg1 = allow ? 200 : 403;
                try {
                  if (msg.replyTo != null) msg.replyTo.send(reply);
                } catch (RemoteException e) {
                  Log.e("A80_37", "reply", e);
                }
                return true;
              }));

  @Override
  public IBinder onBind(Intent i) {
    return messenger.getBinder();
  }
}
