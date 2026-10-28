package com.university.app;

import android.content.*;
import android.util.Log;
import java.nio.charset.StandardCharsets;

public final class SignedReceiver extends BroadcastReceiver {
  public void onReceive(Context c, Intent i) {
    String payload = i.getStringExtra("payload");
    byte[] signature = i.getByteArrayExtra("signature");
    try {
      boolean valid =
          payload != null
              && SignatureTools.verify(payload.getBytes(StandardCharsets.UTF_8), signature);
      Log.i("A80_17", "ECDSA verified=" + valid);
      if (!valid) throw new SecurityException("Invalid signature");
    } catch (Exception e) {
      Log.w("A80_17", "Rejected signed message", e);
    }
  }
}
