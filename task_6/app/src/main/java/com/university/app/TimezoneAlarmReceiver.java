package com.university.app;

import android.app.*;
import android.content.*;
import android.util.Log;
import java.util.*;

public final class TimezoneAlarmReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    Calendar date = Calendar.getInstance();
    date.set(Calendar.HOUR_OF_DAY, 8);
    date.set(Calendar.MINUTE, 30);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MILLISECOND, 0);
    if (date.getTimeInMillis() < System.currentTimeMillis()) date.add(Calendar.DATE, 1);
    PendingIntent fire =
        TaskActions.alarmIntent(
            c, TaskActions.TIMEZONE_ALARM_CODE, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    manager.setAlarmClock(
        new AlarmManager.AlarmClockInfo(date.getTimeInMillis(), TaskActions.openAppIntent(c)),
        fire);
    Log.i("A80_12", "Timezone=" + TimeZone.getDefault().getID() + " alarm=" + date.getTime());
  }
}
