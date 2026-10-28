package com.university.app;

import android.app.Service;
import android.content.Intent;
import android.os.*;

public final class RemoteMessengerService extends Service {
  private final Messenger messenger =
      new Messenger(
          new Handler(
              Looper.getMainLooper(),
              msg -> {
                Message reply = Message.obtain(null, 2);
                reply.arg1 = msg.arg1 * 2;
                try {
                  if (msg.replyTo != null) msg.replyTo.send(reply);
                } catch (RemoteException ignored) {
                }
                return true;
              }));

  @Override
  public IBinder onBind(Intent i) {
    return messenger.getBinder();
  }
}
