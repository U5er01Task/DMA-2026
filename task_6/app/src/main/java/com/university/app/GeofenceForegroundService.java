package com.university.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.location.*;
import android.os.*;
import android.util.Log;

public final class GeofenceForegroundService extends Service implements LocationListener {
  private static final String CHANNEL = "geofence";
  private LocationManager locations;
  private Location center;
  private boolean outside;

  @Override
  public void onCreate() {
    super.onCreate();
    locations = (LocationManager) getSystemService(LOCATION_SERVICE);
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel(CHANNEL, "Геозона", NotificationManager.IMPORTANCE_LOW));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int id) {
    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      stopSelf(id);
      return START_NOT_STICKY;
    }
    Notification n =
        new Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Геозона 500 м")
            .setContentText("Отслеживание активно")
            .setOngoing(true)
            .build();
    if (Build.VERSION.SDK_INT >= 29)
      startForeground(231, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
    else startForeground(231, n);
    try {
      locations.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, this);
      locations.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
    } catch (RuntimeException e) {
      Log.e("A80_5", "Location", e);
      stopSelf(id);
    }
    return START_STICKY;
  }

  @Override
  public void onLocationChanged(Location loc) {
    if (center == null) {
      center = new Location(loc);
      Log.i("A80_5", "warehouse set to first fix");
      return;
    }
    float distance = center.distanceTo(loc);
    Log.i("A80_5", "meters=" + distance);
    if (distance > 500 && !outside) {
      outside = true;
      android.media.RingtoneManager.getRingtone(
              this,
              android.media.RingtoneManager.getDefaultUri(
                  android.media.RingtoneManager.TYPE_NOTIFICATION))
          .play();
      Log.w("A80_5", "EXIT");
    }
    if (distance <= 500) outside = false;
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    locations.removeUpdates(this);
    super.onDestroy();
  }
}
