package com.university.app;

import android.app.Service;
import android.content.*;
import android.hardware.*;
import android.os.*;
import android.util.Log;
import java.util.*;

public final class SensorBoundService extends Service implements SensorEventListener {
  public interface Listener {
    void onNewReading(float x);
  }

  private final IBinder binder = new LocalBinder();
  private final Set<Listener> listeners = new HashSet<>();
  private SensorManager manager;

  @Override
  public void onCreate() {
    super.onCreate();
    manager = (SensorManager) getSystemService(SENSOR_SERVICE);
    Sensor accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    if (accel != null) manager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
    else Log.w("A80_35", "No accelerometer");
  }

  public final class LocalBinder extends Binder {
    public SensorBoundService getService() {
      return SensorBoundService.this;
    }
  }

  public void addListener(Listener l) {
    listeners.add(l);
  }

  public void removeListener(Listener l) {
    listeners.remove(l);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    float x = event.values[0];
    for (Listener l : new ArrayList<>(listeners)) l.onNewReading(x);
    Log.i("A80_35", "accelerometer_x=" + x);
  }

  @Override
  public void onAccuracyChanged(Sensor s, int accuracy) {}

  @Override
  public void onDestroy() {
    manager.unregisterListener(this);
    listeners.clear();
    super.onDestroy();
  }
}
