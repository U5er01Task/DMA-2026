package com.university.app;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import java.util.Random;

public final class RandomService extends Service {
  private int activeClients;
  private final IBinder binder = new LocalBinder();
  private final Random random = new Random();

  public final class LocalBinder extends Binder {
    public RandomService getService() {
      return RandomService.this;
    }
  }

  public int getRandomNumber() {
    return random.nextInt(10000);
  }

  public synchronized void registerClient() {
    activeClients++;
    Log.i("A80_2_4", "clients=" + activeClients);
  }

  public synchronized void unregisterClient() {
    activeClients = Math.max(0, activeClients - 1);
    Log.i("A80_2_4", "clients=" + activeClients);
  }

  @Override
  public IBinder onBind(Intent i) {
    Log.i("A80_2_4", "bind");
    return binder;
  }

  @Override
  public boolean onUnbind(Intent i) {
    Log.i("A80_2_3", "onUnbind");
    return true;
  }

  @Override
  public void onRebind(Intent i) {
    Log.i("A80_2_3", "onRebind");
    super.onRebind(i);
  }
}
