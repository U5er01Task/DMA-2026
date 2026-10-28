package com.university.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.hardware.*;
import android.os.*;
import android.util.Log;

public final class StepForegroundService extends Service implements SensorEventListener {
  private static final String CHANNEL = "steps";
  private SensorManager manager;
  private Sensor counter;
  private float steps;
  private Handler handler;
  private final Runnable refresh =
      new Runnable() {
        public void run() {
          show(false);
          handler.postDelayed(this, 10000);
        }
      };

  @Override
  public void onCreate() {
    super.onCreate();
    manager = (SensorManager) getSystemService(SENSOR_SERVICE);
    handler = new Handler(Looper.getMainLooper());
    if (Build.VERSION.SDK_INT >= 26)
      getSystemService(NotificationManager.class)
          .createNotificationChannel(
              new NotificationChannel(CHANNEL, "Шагомер", NotificationManager.IMPORTANCE_LOW));
  }

  @Override
  public int onStartCommand(Intent i, int flags, int id) {
    if (Build.VERSION.SDK_INT >= 29
        && checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
      stopSelf(id);
      return START_NOT_STICKY;
    }
    show(true);
    counter = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    if (counter != null) manager.registerListener(this, counter, SensorManager.SENSOR_DELAY_NORMAL);
    else Log.w("A80_1", "No step sensor");
    handler.postDelayed(refresh, 10000);
    return START_STICKY;
  }

  private void show(boolean first) {
    Notification n =
        new Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Шагомер")
            .setContentText("Шагов с момента перезагрузки: " + (long) steps)
            .setOngoing(true)
            .build();
    if (first) {
      if (Build.VERSION.SDK_INT >= 34)
        startForeground(230, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
      else startForeground(230, n);
    } else getSystemService(NotificationManager.class).notify(230, n);
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    steps = event.values[0];
    Log.i("A80_1", "step_counter=" + (long) steps);
  }

  @Override
  public void onAccuracyChanged(Sensor s, int accuracy) {}

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacks(refresh);
    manager.unregisterListener(this);
    super.onDestroy();
  }
}
