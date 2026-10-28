package com.university.app;

import android.app.*;
import android.content.*;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.*;
import android.provider.CallLog;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.TextView;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class SecurityAndSystemScenarios {
  private SecurityAndSystemScenarios() {}

  private static final String ALIAS = "A80_SETTINGS_AES";

  public static void aesSettings(Activity activity, TextView view) {
    try {
      KeyStore store = KeyStore.getInstance("AndroidKeyStore");
      store.load(null);
      if (!store.containsAlias(ALIAS)) {
        KeyGenerator generator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(
            new KeyGenParameterSpec.Builder(
                    ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());
        generator.generateKey();
      }
      SecretKey key = ((KeyStore.SecretKeyEntry) store.getEntry(ALIAS, null)).getSecretKey();
      Cipher encrypt = Cipher.getInstance("AES/GCM/NoPadding");
      encrypt.init(Cipher.ENCRYPT_MODE, key);
      byte[] data = encrypt.doFinal("secret-value".getBytes(StandardCharsets.UTF_8));
      activity
          .getSharedPreferences("secure", 0)
          .edit()
          .putString("cipher", Base64.getEncoder().encodeToString(data))
          .putString("iv", Base64.getEncoder().encodeToString(encrypt.getIV()))
          .apply();
      byte[] cipher =
          Base64.getDecoder()
              .decode(activity.getSharedPreferences("secure", 0).getString("cipher", ""));
      byte[] iv =
          Base64.getDecoder()
              .decode(activity.getSharedPreferences("secure", 0).getString("iv", ""));
      Cipher decrypt = Cipher.getInstance("AES/GCM/NoPadding");
      decrypt.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
      String result = new String(decrypt.doFinal(cipher), StandardCharsets.UTF_8);
      Log.i("A80_25", "AES/GCM roundtrip OK");
      view.setText("AES/GCM roundtrip: " + result);
    } catch (Exception error) {
      Log.e("A80_25", "crypto", error);
      view.setText(error.toString());
    }
  }

  public static void headset(Activity a) {
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context c, Intent i) {
            if (i.getIntExtra("state", 1) == 0) {
              Log.i("A80_16", "headset unplugged");
              if (!MediaPlayerService.running) {
                Log.i("A80_16", "player not running");
                return;
              }
              c.startService(
                  new Intent(c, MediaPlayerService.class).setAction(MediaPlayerService.PAUSE));
            }
          }
        };
    IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    a.registerReceiver(receiver, filter);
    TaskActions.extraReceivers.add(receiver);
    TaskActions.trackReceiver(receiver);
  }

  public static void charging(Activity a) {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_POWER_CONNECTED);
    filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          public void onReceive(Context c, Intent intent) {
            Intent battery =
                c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int plugged =
                battery == null ? 0 : battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            String type =
                (plugged & BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0
                    ? "wireless"
                    : (plugged & BatteryManager.BATTERY_PLUGGED_USB) != 0
                        ? "USB"
                        : (plugged & BatteryManager.BATTERY_PLUGGED_AC) != 0 ? "AC" : "battery";
            int current =
                ((BatteryManager) c.getSystemService(Context.BATTERY_SERVICE))
                    .getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            boolean fast = plugged != 0 && Math.abs((long) current) > 1500000;
            Log.i(
                "A80_11",
                "charging=" + type + " current_uA=" + current + " fast_heuristic=" + fast);
          }
        };
    a.registerReceiver(receiver, filter);
    TaskActions.extraReceivers.add(receiver);
    TaskActions.trackReceiver(receiver);
  }

  public static void signed(Activity a) {
    Intent i = new Intent(a, SignedReceiver.class).setAction("com.university.app.SIGNED");
    String payload = "payload";
    try {
      i.putExtra("payload", payload);
      i.putExtra("signature", SignatureTools.sign(payload.getBytes(StandardCharsets.UTF_8)));
      a.sendBroadcast(i, "com.university.app.permission.SIGNED_ALERT");
      Log.i("A80_17", "ECDSA signed message sent");
    } catch (Exception e) {
      Log.e("A80_17", "sign failed", e);
    }
  }

  public static void simulateMaliciousPath() {
    try {
      Uri uri = Uri.parse("content://com.university.app.files/shared/%2E%2E/%2E%2E/secrets.txt");
      String path = Uri.decode(uri.getEncodedPath());
      if (path.contains("../") || path.contains("..\\"))
        throw new SecurityException("path traversal");
      Log.e("A80_42", "incorrectly allowed");
    } catch (SecurityException expected) {
      Log.i("A80_42", "rejected: " + expected.getMessage());
    }
  }
}
