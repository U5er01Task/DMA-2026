package com.university.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.*;
import android.graphics.*;
import android.hardware.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.Log;
import android.widget.*;
import androidx.core.content.FileProvider;
import androidx.work.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public final class TaskActions {
  private TaskActions() {}

  static final String PKG = "com.university.app";
  // для кнопок «■ Стоп»
  private static final boolean[] started = new boolean[80];
  private static final Map<Integer, List<Future<?>>> jobs = new ConcurrentHashMap<>();
  private static final Map<Integer, List<BroadcastReceiver>> taskReceivers = new HashMap<>();
  private static final Map<Integer, List<ContentObserver>> taskObservers = new HashMap<>();
  private static final Map<Integer, List<ServiceConnection>> taskConnections = new HashMap<>();
  private static int currentTask = -1;

  static final int ALARM_CODE = 39;
  static final int TIMEZONE_ALARM_CODE = 123;
  // задания с общим MediaPlayerService
  private static final int[] PLAYER_TASKS = {1, 31, 63};

  static void markStarted(int n) {
    started[n] = true;
  }

  static boolean isStarted(int n) {
    return n >= 0 && n < started.length && started[n];
  }

  static void trackReceiver(BroadcastReceiver r) {
    if (currentTask >= 0)
      taskReceivers.computeIfAbsent(currentTask, k -> new ArrayList<>()).add(r);
  }

  static void trackObserver(ContentObserver o) {
    if (currentTask >= 0)
      taskObservers.computeIfAbsent(currentTask, k -> new ArrayList<>()).add(o);
  }

  static void trackConnection(ServiceConnection c) {
    if (currentTask >= 0)
      taskConnections.computeIfAbsent(currentTask, k -> new ArrayList<>()).add(c);
  }

  // «■ Стоп»: останавливает службы, Worker, ресиверы, наблюдатели и привязки задания.
  // Разовые действия (запрос в БД, диалог) отменить нельзя, они только сбрасываются.
  static void cancel(Activity a, int zero) {
    started[zero] = false;
    currentTask = -1;

    List<Future<?>> futures = jobs.remove(zero);
    if (futures != null) for (Future<?> f : futures) f.cancel(true);

    detachTask(a, zero);
    if (isPlayerTask(zero)) {
      // пока к плееру привязано хоть одно задание, служба не завершится
      for (int id : PLAYER_TASKS) {
        started[id] = false;
        detachTask(a, id);
      }
    }

    Class<? extends Service> service = serviceFor(zero);
    if (service != null) a.stopService(new Intent(a, service));

    switch (zero) {
      case 17:
        WorkManager.getInstance(a).cancelAllWorkByTag("A80_Worker");
        break;
      case 74:
        WorkManager.getInstance(a).cancelAllWorkByTag("doze");
        break;
      case 41:
        cancelAlarm(a, TIMEZONE_ALARM_CODE);
        stopAlarmScreen(a);
        break;
      case 68:
        cancelAlarm(a, ALARM_CODE);
        stopAlarmScreen(a);
        break;
      case 44:
        a.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("autostart", false)
            .apply();
        break;
      case 63:
        // после MUTE возвращаем громкость
        ((AudioManager) a.getSystemService(Context.AUDIO_SERVICE))
            .adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        break;
      case 69:
        a.getPackageManager()
            .setComponentEnabledSetting(
                new ComponentName(a, ToggleReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP);
        break;
      case 77:
        if (sensors != null && sensorListener != null) {
          sensors.unregisterListener(sensorListener);
          sensorListener = null;
        }
        break;
      default:
        break;
    }
    log(zero + 1, "STOP");
  }

  private static boolean isPlayerTask(int zero) {
    for (int id : PLAYER_TASKS) if (id == zero) return true;
    return false;
  }

  private static void detachTask(Activity a, int zero) {
    List<BroadcastReceiver> receivers = taskReceivers.remove(zero);
    if (receivers != null)
      for (BroadcastReceiver r : receivers) {
        try {
          a.unregisterReceiver(r);
        } catch (IllegalArgumentException ignored) {
        }
        extraReceivers.remove(r);
        if (receiver == r) receiver = null;
        if (screenReceiver == r) screenReceiver = null;
      }
    List<ContentObserver> observers = taskObservers.remove(zero);
    if (observers != null)
      for (ContentObserver o : observers) {
        a.getContentResolver().unregisterContentObserver(o);
        extraObservers.remove(o);
        if (observer == o) observer = null;
      }
    List<ServiceConnection> connections = taskConnections.remove(zero);
    if (connections != null)
      for (ServiceConnection c : connections) {
        try {
          a.unbindService(c);
        } catch (IllegalArgumentException ignored) {
        }
        otherConnections.remove(c);
        if (connection == c) {
          if (boundRandom != null) boundRandom.unregisterClient();
          connection = null;
          bound = false;
          boundRandom = null;
        }
      }
  }

  static PendingIntent alarmIntent(Context c, int code, int flags) {
    return PendingIntent.getBroadcast(
        c, code, new Intent(c, AlarmReceiver.class), PendingIntent.FLAG_IMMUTABLE | flags);
  }

  // showIntent для AlarmClockInfo должен открывать Activity
  static PendingIntent openAppIntent(Context c) {
    return PendingIntent.getActivity(
        c,
        0,
        new Intent(c, MainActivity.class),
        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private static void cancelAlarm(Activity a, int code) {
    PendingIntent pi = alarmIntent(a, code, PendingIntent.FLAG_NO_CREATE);
    if (pi != null) {
      ((AlarmManager) a.getSystemService(Context.ALARM_SERVICE)).cancel(pi);
      pi.cancel();
    }
  }

  private static void stopAlarmScreen(Activity a) {
    a.stopService(new Intent(a, AlarmForegroundService.class));
    a.sendBroadcast(
        new Intent(AlarmScreenActivity.ACTION_DISMISS).setPackage(a.getPackageName()));
  }

  private static Class<? extends Service> serviceFor(int task) {
    switch (task) {
      case 0:
        return DemoService.class;
      case 1:
      case 31:
      case 63:
        return MediaPlayerService.class;
      case 2:
        return SingleFileTransferService.class;
      case 3:
        return DownloaderService.class;
      case 4:
      case 34:
        return GeofenceForegroundService.class;
      case 5:
      case 7:
      case 8:
      case 9:
        return RandomService.class;
      case 6:
        return StickyDemoService.class;
      case 30:
        return StepForegroundService.class;
      case 32:
      case 60:
        return DownloadForegroundService.class;
      case 33:
        return WebSocketForegroundService.class;
      case 35:
        return VideoTranscodeService.class;
      case 36:
        return RemoteMessengerService.class;
      case 37:
        return CacheCleanupService.class;
      case 38:
        return ClipboardMonitorService.class;
      case 39:
      case 44:
        return TimedForegroundService.class;
      case 58:
        return CallLogService.class;
      case 73:
        return LeakProbeService.class;
      case 62:
        return CacheFetchService.class;
      case 64:
        return SensorBoundService.class;
      case 65:
        return BackupService.class;
      case 67:
        return PipeWriterService.class;
      case 68:
        return AlarmForegroundService.class;
      case 79:
        return EnterpriseSyncService.class;
      default:
        return null;
    }
  }

  static final ExecutorService IO = Executors.newSingleThreadExecutor();
  static BroadcastReceiver receiver;
  static ContentObserver observer;
  static ServiceConnection connection;
  static boolean bound;
  static RandomService boundRandom;
  static final java.util.List<BroadcastReceiver> extraReceivers = new java.util.ArrayList<>();
  static final java.util.List<ContentObserver> extraObservers = new java.util.ArrayList<>();
  static SensorEventListener sensorListener;
  static SensorManager sensors;

  static void log(int n, String value) {
    Log.i("A80_" + n, value);
  }

  static void text(TextView t, String value) {
    t.setText(value);
  }

  static void launch(Activity a, String action) {
    Intent i = new Intent(a, DemoService.class).setAction(action);
    if (Build.VERSION.SDK_INT >= 26) a.startForegroundService(i);
    else a.startService(i);
  }

  static void note(Activity a, String title, String category) {
    ContentValues v = new ContentValues();
    v.put("title", title);
    v.put("category", category);
    Uri result = a.getContentResolver().insert(NotesProvider.URI, v);
    log(100, "Insert " + result);
  }

  static void query(Activity a, TextView t, Uri uri, String selection, String[] args) {
    StringBuilder b = new StringBuilder();
    try (Cursor c = a.getContentResolver().query(uri, null, selection, args, "_id DESC")) {
      if (c != null) {
        while (c.moveToNext() && b.length() < 3000) {
          b.append(c.getLong(0)).append(" ").append(c.getString(1)).append("\n");
        }
      }
    }
    text(t, b.toString().isEmpty() ? "Нет записей" : b.toString());
  }

  static void register(Activity a, String action, int id) {
    if (receiver != null) {
      try {
        a.unregisterReceiver(receiver);
      } catch (Exception ignored) {
      }
    }
    receiver =
        new BroadcastReceiver() {
          public void onReceive(Context c, Intent i) {
            log(
                id,
                "received="
                    + i.getAction()
                    + " main="
                    + (Looper.myLooper() == Looper.getMainLooper())
                    + " extras="
                    + i.getExtras());
            if (Intent.ACTION_BATTERY_LOW.equals(i.getAction()))
              Toast.makeText(c, "Внимание: низкий заряд!", Toast.LENGTH_LONG).show();
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(i.getAction()))
              log(id, "airplane=" + i.getBooleanExtra("state", false));
          }
        };
    IntentFilter f = new IntentFilter(action);
    if (Build.VERSION.SDK_INT >= 33) a.registerReceiver(receiver, f, Context.RECEIVER_EXPORTED);
    else a.registerReceiver(receiver, f);
    trackReceiver(receiver);
    log(id, "registered " + action);
  }

  static void permission(Activity a, int n, String p) {
    if (Build.VERSION.SDK_INT >= 23
        && a.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
      ((MainActivity) a).requestFor(n, p);
      throw new SecurityException("Requesting " + p);
    }
  }

  static void runAsync(int id, Runnable runnable) {
    Future<?> future = IO.submit(
        () -> {
          try {
            runnable.run();
            log(id, "done");
          } catch (Exception e) {
            Log.e("A80_" + id, "failure", e);
          }
        });
    int owner = currentTask >= 0 ? currentTask : id - 1;
    jobs.computeIfAbsent(owner, k -> new CopyOnWriteArrayList<>()).add(future);
  }

  static void clearCurrentTask() { currentTask = -1; }

  static void perform(Activity a, int zero, TextView output) {
    final int n = zero + 1;
    currentTask = zero;
    log(n, "START");
    text(
        output,
        "Задание " + (zero < 30 ? "T" + (zero / 5 + 1) + "." + (zero % 5 + 1) : "№" + (zero - 29)));
    switch (zero) {
      case 0:
        a.startService(new Intent(a, DemoService.class).setAction(DemoService.ACTION_COUNT));
        break;
      case 1:
      case 31:
        {
          Intent i = new Intent(a, MediaPlayerService.class).setAction(MediaPlayerService.PLAY);
          androidx.core.content.ContextCompat.startForegroundService(a, i);
          if (zero == 31) bindPlayer(a, output);
          break;
        }
      case 2:
        a.startService(new Intent(a, SingleFileTransferService.class));
        break;
      case 3:
        a.startService(new Intent(a, DownloaderService.class));
        break;
      case 4:
        if (Build.VERSION.SDK_INT >= 33)
          permission(a, zero, Manifest.permission.POST_NOTIFICATIONS);
        permission(a, zero, Manifest.permission.ACCESS_FINE_LOCATION);
        startForeground(a, GeofenceForegroundService.class);
        break;
      case 5:
      case 9:
        a.startService(new Intent(a, RandomService.class));
        bindRandom(a, output, zero);
        break;
      case 6:
        a.startService(new Intent(a, StickyDemoService.class));
        break;
      case 7:
        a.startService(new Intent(a, RandomService.class));
        bindRandom(a, output, zero);
        break;
      case 8:
        bindRandom(a, output, zero);
        a.startActivity(new Intent(a, ClientScreenActivity.class));
        break;
      case 10:
        register(a, Intent.ACTION_BATTERY_LOW, n);
        break;
      case 11:
        register(a, Intent.ACTION_AIRPLANE_MODE_CHANGED, n);
        break;
      case 12:
        a.sendBroadcast(new Intent(a, BootDemoReceiver.class).setAction(BootDemoReceiver.TEST_BOOT));
        break;
      case 13:
        a.sendBroadcast(
            new Intent(a, ToggleReceiver.class).setAction("com.university.app.CUSTOM_ALERT"));
        break;
      case 14:
        ordered(a, true);
        break;
      case 15:
        a.sendBroadcast(new Intent(a, UiThreadReceiver.class));
        break;
      case 16:
        new AlertDialog.Builder(a)
            .setTitle("Учебный ANR")
            .setMessage("Блокировать onReceive на 15 секунд?")
            .setPositiveButton(
                "ANR", (d, w) -> a.sendBroadcast(new Intent(a, AnrDemoReceiver.class)))
            .setNegativeButton("Отмена", null)
            .show();
        break;
      case 17:
        a.sendBroadcast(
            new Intent(a, WorkEnqueueReceiver.class).setAction(WorkEnqueueReceiver.ACTION));
        break;
      case 18:
        a.sendBroadcast(
            new Intent(a, AsyncPreferenceReceiver.class)
                .setAction("com.university.app.SAVE_EVENT"));
        break;
      case 19:
        register(a, Intent.ACTION_BATTERY_LOW, n);
        a.unregisterReceiver(receiver);
        receiver = null;
        log(n, "unregistered");
        // та же ошибка на практике: Activity без unregisterReceiver()
        a.startActivity(new Intent(a, LeakDemoActivity.class));
        break;
      case 20:
        permission(a, zero, Manifest.permission.READ_CONTACTS);
        readContacts(a);
        break;
      case 21:
        if (Build.VERSION.SDK_INT >= 33) permission(a, zero, Manifest.permission.READ_MEDIA_IMAGES);
        readImages(a);
        break;
      case 22:
        note(a, "Example category", "student");
        query(a, output, Uri.withAppendedPath(NotesProvider.URI, "category/student"), null, null);
        break;
      case 23:
        try {
          note(a, "", "student");
        } catch (IllegalArgumentException e) {
          log(n, "validation rejected " + e.getMessage());
        }
        break;
      case 24:
        watch(a, output);
        break;
      case 25:
        log(n, "Порядок старта процесса смотри по строкам A80_6_1: Provider, Application, Activity");
        break;
      case 26:
        note(a, "sync insert", "thread");
        log(n, "CRUD synchronized");
        break;
      case 27:
        applyBatch(a, output);
        break;
      case 28:
        shareFile(a, "example.pdf", "application/pdf");
        break;
      case 29:
        note(a, "safe SQL", "a");
        query(a, output, NotesProvider.URI, "title=?", new String[] {"' OR 1=1 --"});
        break;
      case 30:
        permission(a, zero, Manifest.permission.ACTIVITY_RECOGNITION);
        startForeground(a, StepForegroundService.class);
        break;
      case 32:
        download(a);
        break;
      case 33:
        {
          String url = a.getPreferences(0).getString("socket_url", "");
          if (url.isEmpty()) {
            android.widget.EditText edit = new android.widget.EditText(a);
            edit.setHint("wss://server.example/ws");
            new AlertDialog.Builder(a)
                .setTitle("WebSocket URL")
                .setView(edit)
                .setPositiveButton(
                    "Start",
                    (d, w) -> {
                      if (!isStarted(33)) return;
                      String endpoint = edit.getText().toString().trim();
                      a.getPreferences(0).edit().putString("socket_url", endpoint).apply();
                      Intent i =
                          new Intent(a, WebSocketForegroundService.class)
                              .putExtra(WebSocketForegroundService.EXTRA_URL, endpoint);
                      androidx.core.content.ContextCompat.startForegroundService(a, i);
                    })
                .setNegativeButton("Cancel", null)
                .show();
          } else {
            Intent i =
                new Intent(a, WebSocketForegroundService.class)
                    .putExtra(WebSocketForegroundService.EXTRA_URL, url);
            androidx.core.content.ContextCompat.startForegroundService(a, i);
          }
          break;
        }

      case 36:
        messenger(a, output);
        break;
      case 35:
        {
          Intent picker =
              new Intent(Intent.ACTION_OPEN_DOCUMENT)
                  .setType("video/*")
                  .addCategory(Intent.CATEGORY_OPENABLE);
          a.startActivityForResult(picker, 106);
          break;
        }
      case 34:
        permission(a, zero, Manifest.permission.ACCESS_FINE_LOCATION);
        startForeground(a, GeofenceForegroundService.class);
        break;
      case 37:
        a.startService(new Intent(a, CacheCleanupService.class));
        break;
      case 38:
        if (Build.VERSION.SDK_INT >= 33)
          permission(a, zero, Manifest.permission.POST_NOTIFICATIONS);
        startForeground(a, ClipboardMonitorService.class);
        break;
      case 39:
        startForeground(a, TimedForegroundService.class);
        break;
      case 40:
        SecurityAndSystemScenarios.charging(a);
        break;
      case 41:
        a.sendBroadcast(
            new Intent(a, TimezoneAlarmReceiver.class).setAction(Intent.ACTION_TIMEZONE_CHANGED));
        break;
      case 42:
        permission(a, zero, Manifest.permission.SEND_SMS);
        ownSmsDemo(a);
        break;
      case 43:
        a.sendBroadcast(
            new Intent(a, EventReceiver.class).setAction("com.university.app.TEST_STORAGE_LOW"));
        break;
      case 44:
        // включаем автозапуск в настройках
        a.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("autostart", true)
            .apply();
        a.sendBroadcast(
            new Intent(a, BootDemoReceiver.class).setAction(BootDemoReceiver.TEST_AUTOSTART));
        break;
      case 45:
        SecurityAndSystemScenarios.headset(a);
        break;
      case 46:
        SecurityAndSystemScenarios.signed(a);
        break;
      case 47:
        a.sendBroadcast(
            new Intent(a, LocaleRefreshReceiver.class).setAction(Intent.ACTION_LOCALE_CHANGED));
        break;
      case 48:
        ordered(a, false);
        break;
      case 49:
        a.sendBroadcast(
            new Intent(a, TimeoutReceiver.class)
                .setAction("com.university.app.TIMEOUT")
                .putExtra("delay", 12000L));
        break;
      case 50:
        {
          ContentValues v = new ContentValues();
          v.put("message", "Confidential analytics log");
          Uri stored = a.getContentResolver().insert(EncryptedLogsProvider.URI, v);
          log(n, "encrypted=" + stored);
          try (Cursor c =
              a.getContentResolver().query(EncryptedLogsProvider.URI, null, null, null, null)) {
            text(output, "Encrypted logs: " + (c == null ? 0 : c.getCount()));
          }
          break;
        }
      case 51:
        {
          note(a, "Searching suggestion", "public_search");
          Uri sug =
              Uri.parse(
                  "content://"
                      + SearchSuggestProvider.AUTHORITY
                      + "/"
                      + android.app.SearchManager.SUGGEST_URI_PATH_QUERY
                      + "/Searching");
          try (Cursor c = a.getContentResolver().query(sug, null, null, null, null)) {
            StringBuilder builder = new StringBuilder("Suggestions:\n");
            while (c != null && c.moveToNext()) builder.append(c.getString(1)).append("\n");
            text(output, builder.toString());
          }
          break;
        }
      case 52:
        shareFile(a, "contact.vcf", "text/vcard");
        break;
      case 53:
        note(a, "Shopping cart", "cart");
        query(a, output, NotesProvider.URI, "category=?", new String[] {"cart"});
        break;
      case 54:
        SecurityAndSystemScenarios.aesSettings(a, output);
        break;

      case 55:
        takePhoto(a);
        break;
      case 56:
        bulk(a, output);
        break;
      case 57:
        {
          Uri u = ReportsProvider.BASE.buildUpon().appendQueryParameter("format", "csv").build();
          log(
              n,
              "stream types="
                  + java.util.Arrays.toString(a.getContentResolver().getStreamTypes(u, "*/*")));
          openReport(a, u, "text/csv");
          break;
        }
      case 58:
        permission(a, zero, Manifest.permission.READ_CALL_LOG);
        a.startService(new Intent(a, CallLogService.class));
        break;
      case 59:
        note(a, "Search full text", "fts");
        query(
            a,
            output,
            NotesProvider.URI
                .buildUpon()
                .appendPath("search")
                .appendQueryParameter("q", "Search")
                .build(),
            null,
            null);
        break;
      case 60:
        download(a);
        break;
      case 61:
        a.sendBroadcast(
            new Intent(a, BatterySafetyReceiver.class)
                .setAction("com.university.app.TEST_LOW_BATTERY"));
        break;
      case 62:
        cacheProxy(a, output);
        break;
      case 63:
        a.sendBroadcast(
            new Intent(a, PlaybackControlReceiver.class).setAction("com.university.app.PLAY"));
        break;
      case 64:
        sensorCallbacks(a, output);
        break;
      case 65:
        {
          BroadcastReceiver tick =
              new BroadcastReceiver() {
                public void onReceive(Context c, Intent i) {
                  log(36, "ACTION_TIME_TICK -> BackupService");
                  a.startService(new Intent(a, BackupService.class));
                }
              };
          a.registerReceiver(tick, new IntentFilter(Intent.ACTION_TIME_TICK));
          extraReceivers.add(tick);
          trackReceiver(tick);
          a.startService(new Intent(a, BackupService.class));
          break;
        }
      case 66:
        checkWhitelist(a, output);
        break;
      case 67:
        providerPipe(a, n, output);
        break;
      case 68:
        alarm(a);
        break;
      case 69:
        toggleComponent(a);
        break;
      case 70:
        replay(a);
        break;
      case 71:
        {
          Uri attack = Uri.parse("content://com.university.app.safefiles/file/%2E%2E%2Fsecret.txt");
          try (ParcelFileDescriptor fd = a.getContentResolver().openFileDescriptor(attack, "r")) {
            log(n, "ERROR: attack accepted");
          } catch (Exception e) {
            log(n, "Path traversal blocked: " + e);
          }
          break;
        }
      case 72:
        {
          ProviderInfo info =
              a.getPackageManager().resolveContentProvider(NotesProvider.AUTHORITY, 0);
          if (info != null) {
            log(n, "readPermission=" + info.readPermission);
            log(n, "writePermission=" + info.writePermission);
            text(output, "read: " + info.readPermission + "\nwrite: " + info.writePermission);
          }
        }
        break;
      case 73:
        {
          Runtime rt = Runtime.getRuntime();
          long before = rt.totalMemory() - rt.freeMemory();
          System.gc();
          long after = rt.totalMemory() - rt.freeMemory();
          log(n, "heap до gc=" + before / 1024 + " КБ, после=" + after / 1024 + " КБ");
          try {
            File dump = new File(a.getExternalFilesDir(null), "services.hprof");
            Debug.dumpHprofData(dump.getAbsolutePath());
            log(n, "heap dump: " + dump.getAbsolutePath());
          } catch (Exception e) {
            Log.e("A80_" + n, "hprof", e);
          }
          text(output, "heap после gc: " + after / 1024 + " КБ");
          a.startActivity(new Intent(a, StaticLeakActivity.class));
        }
        break;
      case 74:
        WorkManager.getInstance(a)
            .enqueue(new OneTimeWorkRequest.Builder(DozeWorker.class).addTag("doze").build());
        break;
      case 75:
        try {
          ServiceInfo si =
              a.getPackageManager().getServiceInfo(new ComponentName(a, DemoService.class), 0);
          log(n, "DemoService exported=" + si.exported);
          text(output, "DemoService exported=" + si.exported);
        } catch (PackageManager.NameNotFoundException e) {
          Log.e("A80_" + n, "service info", e);
        }
        break;
      case 76:
        runAsync(n, () -> CorruptDatabaseDemo.run(a));
        break;
      case 77:
        monitorAccel(a);
        break;
      case 78:
        editImage(a);
        break;
      case 79:
        startForeground(a, EnterpriseSyncService.class);
        text(output, "dataSync запущен: результаты в Logcat A80_50");
        break;
      default:
        log(n, "Device interaction or independent test required");
    }
    currentTask = -1;
  }

  static void ownSmsDemo(Activity a) {
    android.widget.LinearLayout layout = new android.widget.LinearLayout(a);
    layout.setOrientation(1);
    android.widget.EditText phone = new android.widget.EditText(a);
    phone.setHint("Phone number (SMS may cost money)");
    phone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
    android.widget.EditText message = new android.widget.EditText(a);
    message.setHint("SMS test message");
    layout.addView(phone);
    layout.addView(message);
    new AlertDialog.Builder(a)
        .setTitle("Собственный SMS_SENT — не перехват чужих SMS")
        .setView(layout)
        .setNegativeButton("Отмена", null)
        .setPositiveButton(
            "Отправить SMS",
            (d, w) -> {
              String destination = phone.getText().toString().trim();
              String text = message.getText().toString();
              if (destination.length() < 4 || text.isEmpty()) return;
              Intent status =
                  new Intent(a, SmsSentReceiver.class).setAction("com.university.app.SMS_SENT");
              PendingIntent sent =
                  PendingIntent.getBroadcast(
                      a,
                      13,
                      status,
                      PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
              try {
                android.telephony.SmsManager.getDefault()
                    .sendTextMessage(destination, null, text, sent, null);
              } catch (Exception e) {
                Log.e("A80_13", "SMS send failed", e);
              }
            })
        .show();
  }

  static void bindPlayer(Activity a, TextView output) {
    ServiceConnection c =
        new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName cn, IBinder binder) {
            MediaPlayerService service = ((MediaPlayerService.LocalBinder) binder).getService();
            output.setText("Плеер: " + service.getTrackName());
            log(32, "Bound media player connected");
          }

          @Override
          public void onServiceDisconnected(ComponentName cn) {
            log(32, "media player disconnected");
          }
        };
    a.bindService(new Intent(a, MediaPlayerService.class), c, Context.BIND_AUTO_CREATE);
    otherConnections.add(c);
    trackConnection(c);
  }

  static void cacheProxy(Activity a, TextView t) {
    ContentObserver o =
        new ContentObserver(new Handler(Looper.getMainLooper())) {
          @Override
          public void onChange(boolean self) {
            query(a, t, CacheProxyProvider.URI, null, null);
            Log.i("A80_33", "notifyChange UI refreshed");
          }
        };
    a.getContentResolver().registerContentObserver(CacheProxyProvider.URI, true, o);
    extraObservers.add(o);
    trackObserver(o);
    query(a, t, CacheProxyProvider.URI, null, null);
  }

  static void sensorCallbacks(Activity a, TextView output) {
    ServiceConnection c =
        new ServiceConnection() {
          SensorBoundService.Listener listener;
          SensorBoundService svc;

          @Override
          public void onServiceConnected(ComponentName cn, IBinder b) {
            svc = ((SensorBoundService.LocalBinder) b).getService();
            listener = v -> output.setText("Датчик: " + v);
            svc.addListener(listener);
            log(65, "callback attached");
          }

          @Override
          public void onServiceDisconnected(ComponentName cn) {
            if (svc != null && listener != null) svc.removeListener(listener);
          }
        };
    a.bindService(new Intent(a, SensorBoundService.class), c, Context.BIND_AUTO_CREATE);
    otherConnections.add(c);
    trackConnection(c);
  }

  static void providerPipe(Activity a, int id, TextView output) {
    runAsync(
        id,
        () -> {
          try (java.io.InputStream in =
              a.getContentResolver().openInputStream(StreamProvider.URI)) {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = in.read(b)) != -1) bytes.write(b, 0, len);
            String received = bytes.toString("UTF-8");
            log(id, "provider pipe=" + received);
            a.runOnUiThread(() -> output.setText(received));
          } catch (Exception e) {
            Log.e("A80_38", "stream", e);
          }
        });
  }

  static void startForeground(Activity a, Class<? extends Service> service) {
    androidx.core.content.ContextCompat.startForegroundService(a, new Intent(a, service));
  }

  static final java.util.List<ServiceConnection> otherConnections = new java.util.ArrayList<>();

  static void multiBind(Activity a, TextView output, int id) {
    a.startService(new Intent(a, RandomService.class));
    for (int i = 0; i < 2; i++) {
      final int number = i + 1;
      ServiceConnection c =
          new ServiceConnection() {
            public void onServiceConnected(ComponentName cn, IBinder b) {
              log(id, "client " + number + " connected");
              output.append("Client " + number + "\n");
            }

            public void onServiceDisconnected(ComponentName cn) {
              log(id, "client " + number + " disconnected");
            }
          };
      a.bindService(new Intent(a, RandomService.class), c, Context.BIND_AUTO_CREATE);
      otherConnections.add(c);
      trackConnection(c);
    }
    log(id, "clients bound=" + otherConnections.size());
  }

  static void checkWhitelist(Activity a, TextView output) {
    ServiceConnection c =
        new ServiceConnection() {
          public void onServiceConnected(ComponentName cn, IBinder binder) {
            Messenger remote = new Messenger(binder);
            Messenger replies =
                new Messenger(
                    new Handler(
                        Looper.getMainLooper(),
                        m -> {
                          log(37, "UID decision: " + m.arg1);
                          output.setText("UID ACL status=" + m.arg1);
                          return true;
                        }));
            Message message = Message.obtain(null, 10);
            message.replyTo = replies;
            try {
              remote.send(message);
            } catch (RemoteException e) {
              Log.e("A80_37", "ACL test", e);
            }
          }

          public void onServiceDisconnected(ComponentName cn) {
            log(37, "acl connection lost");
          }
        };
    a.bindService(new Intent(a, WhitelistedMessengerService.class), c, Context.BIND_AUTO_CREATE);
    otherConnections.add(c);
    trackConnection(c);
  }

  static void messenger(Activity a, TextView t) {
    ServiceConnection c =
        new ServiceConnection() {
          public void onServiceConnected(ComponentName name, IBinder binder) {
            Messenger remote = new Messenger(binder);
            Messenger replies =
                new Messenger(
                    new Handler(
                        Looper.getMainLooper(),
                        m -> {
                          log(37, "remote reply=" + m.arg1);
                          t.setText("IPC remote result=" + m.arg1);
                          return true;
                        }));
            Message m = Message.obtain(null, 1);
            m.arg1 = 21;
            m.replyTo = replies;
            try {
              remote.send(m);
            } catch (RemoteException e) {
              Log.e("A80_7", "IPC", e);
            }
          }

          public void onServiceDisconnected(ComponentName name) {
            log(37, "remote disconnected");
          }
        };
    a.bindService(new Intent(a, RemoteMessengerService.class), c, Context.BIND_AUTO_CREATE);
    otherConnections.add(c);
    trackConnection(c);
  }

  static void bindRandom(Activity a, TextView t, int index) {
    if (bound && connection != null) {
      if (boundRandom != null) boundRandom.unregisterClient();
      boundRandom = null;
      a.unbindService(connection);
      bound = false;
      log(index, "onUnbind");
      return;
    }
    connection =
        new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName c, IBinder binder) {
            RandomService service = ((RandomService.LocalBinder) binder).getService();
            service.registerClient();
            boundRandom = service;
            text(t, "Random=" + service.getRandomNumber());
            log(index, "onBind result=" + service.getRandomNumber());
          }

          @Override
          public void onServiceDisconnected(ComponentName c) {
            log(index, "disconnected");
          }
        };
    bound = a.bindService(new Intent(a, RandomService.class), connection, Context.BIND_AUTO_CREATE);
    if (bound) trackConnection(connection);
  }

  static void ordered(Activity a, boolean abort) {
    Intent i = new Intent("com.university.app.ORDERED");
    i.putExtra("abort", abort);
    i.setPackage(a.getPackageName());
    a.sendOrderedBroadcast(i, null);
    log(15, "ordered sent");
  }

  static void readContacts(Activity a) {
    runAsync(
        21,
        () -> {
          try (Cursor c =
              a.getContentResolver()
                  .query(
                      ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                      new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                      null,
                      null,
                      null)) {
            int count = 0;
            while (c != null && c.moveToNext() && count++ < 20) log(21, c.getString(0));
          }
        });
  }

  static void readImages(Activity a) {
    runAsync(
        22,
        () -> {
          try (Cursor c =
              a.getContentResolver()
                  .query(
                      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                      new String[] {
                        MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE
                      },
                      null,
                      null,
                      null)) {
            int count = 0;
            while (c != null && c.moveToNext() && count++ < 20)
              log(22, c.getString(0) + " " + c.getLong(1));
          }
        });
  }

  static void watch(Activity a, TextView output) {
    if (observer != null) {
      a.getContentResolver().unregisterContentObserver(observer);
      observer = null;
    }
    observer =
        new ContentObserver(new Handler(Looper.getMainLooper())) {
          @Override
          public void onChange(boolean selfChange) {
            log(25, "ContentObserver changed");
            query(a, output, NotesProvider.URI, null, null);
          }
        };
    a.getContentResolver().registerContentObserver(NotesProvider.URI, true, observer);
    trackObserver(observer);
    note(a, "observer trigger", "demo");
  }

  static void applyBatch(Activity a, TextView t) {
    runAsync(
        28,
        () -> {
          try {
            java.util.ArrayList<ContentProviderOperation> operations = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
              ContentValues v = new ContentValues();
              v.put("title", "Tx " + i);
              v.put("category", "batch");
              operations.add(
                  ContentProviderOperation.newInsert(NotesProvider.URI).withValues(v).build());
            }
            ContentProviderResult[] result =
                a.getContentResolver().applyBatch(NotesProvider.AUTHORITY, operations);
            log(28, "applyBatch committed rows=" + result.length);
            a.runOnUiThread(() -> text(t, "Batch: " + result.length));
          } catch (Exception e) {
            Log.e("A80_6_3", "batch error", e);
          }
        });
  }

  static void bulk(Activity a, TextView t) {
    runAsync(
        27,
        () -> {
          ContentValues[] vs = new ContentValues[10000];
          for (int i = 0; i < vs.length; i++) {
            vs[i] = new ContentValues();
            vs[i].put("title", "Bulk " + i);
            vs[i].put("category", "batch");
          }
          long ms = System.currentTimeMillis();
          int count = a.getContentResolver().bulkInsert(NotesProvider.URI, vs);
          log(27, "insert=" + count + " ms=" + (System.currentTimeMillis() - ms));
          a.runOnUiThread(() -> text(t, "Inserted " + count));
        });
  }

  static void editImage(Activity a) {
    try {
      File dir = new File(a.getCacheDir(), "sharing");
      dir.mkdirs();
      File png = new File(dir, "edit.png");
      try (FileOutputStream stream = new FileOutputStream(png)) {
        Bitmap bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(0xFF77AAFF);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bmp.recycle();
      }
      Uri uri = FileProvider.getUriForFile(a, PKG + ".files", png);
      Intent i =
          new Intent(Intent.ACTION_EDIT)
              .setDataAndType(uri, "image/png")
              .addFlags(
                  Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
      a.startActivity(Intent.createChooser(i, "Редактор фото"));
      log(49, "Edit URI=" + uri);
    } catch (Exception e) {
      Log.e("A80_49", "No editor or FileProvider issue", e);
    }
  }

  static void openReport(Activity a, Uri uri, String mime) {
    Intent i =
        new Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, mime)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    try {
      a.startActivity(Intent.createChooser(i, "Отчёт"));
    } catch (Exception e) {
      Log.e("A80_28", "report handler", e);
    }
  }

  static void shareFile(Activity a, String name, String mime) {
    runAsync(
        29,
        () -> {
          try {
            File dir = new File(a.getCacheDir(), "sharing");
            dir.mkdirs();
            File file = new File(dir, name);
            try (FileOutputStream out = new FileOutputStream(file)) {
              if (name.endsWith(".pdf")) out.write(verySmallPdf());
              else if (name.endsWith(".vcf"))
                out.write("BEGIN:VCARD\nVERSION:3.0\nFN:Demo User\nEND:VCARD\n".getBytes());
              else if (name.endsWith(".csv")) out.write("id,title\n1,Example\n".getBytes());
              else {
                Bitmap b = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
                b.recycle();
              }
            }
            Uri u = FileProvider.getUriForFile(a, PKG + ".files", file);
            Intent i =
                new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(u, mime)
                    .addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            a.startActivity(Intent.createChooser(i, "Open"));
          } catch (Exception e) {
            Log.e("A80_FILE", "share", e);
          }
        });
  }

  static byte[] verySmallPdf() throws Exception {
    String stream = "BT /F1 14 Tf 40 80 Td (Android80 PDF) Tj ET";
    String[] pieces = {
      "<< /Type /Catalog /Pages 2 0 R >>",
      "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
      "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 150] /Resources << /Font << /F1 4 0 R >> >>"
          + " /Contents 5 0 R >>",
      "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
      "<< /Length " + stream.length() + " >>\nstream\n" + stream + "\nendstream"
    };
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    b.write("%PDF-1.4\n".getBytes("US-ASCII"));
    int[] offsets = new int[6];
    for (int i = 0; i < pieces.length; i++) {
      offsets[i + 1] = b.size();
      b.write(((i + 1) + " 0 obj\n" + pieces[i] + "\nendobj\n").getBytes("US-ASCII"));
    }
    int start = b.size();
    b.write("xref\n0 6\n0000000000 65535 f \n".getBytes());
    for (int i = 1; i < 6; i++)
      b.write(String.format(java.util.Locale.US, "%010d 00000 n \n", offsets[i]).getBytes());
    b.write(("trailer\n<< /Root 1 0 R /Size 6 >>\nstartxref\n" + start + "\n%%EOF").getBytes());
    return b.toByteArray();
  }

  static void download(Activity a) {
    android.widget.EditText edit = new android.widget.EditText(a);
    edit.setSingleLine(false);
    edit.setText(
        a.getPreferences(0).getString("download_url", "https://example.org/photos/{n}.jpg"));
    new AlertDialog.Builder(a)
        .setTitle("Шаблон URL для 100 файлов: {n}=001...100")
        .setView(edit)
        .setPositiveButton(
            "Скачать",
            (d, w) -> {
              if (!isStarted(32) && !isStarted(60)) return;
              String url = edit.getText().toString().trim();
              a.getPreferences(0).edit().putString("download_url", url).apply();
              Intent i =
                  new Intent(a, DownloadForegroundService.class)
                      .putExtra(DownloadForegroundService.EXTRA_TEMPLATE, url);
              androidx.core.content.ContextCompat.startForegroundService(a, i);
            })
        .setNegativeButton("Отмена", null)
        .show();
  }

  static void takePhoto(Activity a) {
    try {
      File dir = new File(a.getCacheDir(), "sharing");
      dir.mkdirs();
      File f = new File(dir, "camera.jpg");
      Uri u = FileProvider.getUriForFile(a, PKG + ".files", f);
      Intent intent =
          new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
              .putExtra(MediaStore.EXTRA_OUTPUT, u)
              .addFlags(
                  Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
      a.startActivityForResult(intent, 53);
    } catch (Exception e) {
      Log.e("A80_26", "camera", e);
    }
  }

  static BroadcastReceiver screenReceiver;

  static void monitorAccel(Activity a) {
    SensorManager manager = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
    Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    if (sensor == null) {
      log(48, "accelerometer missing");
      return;
    }
    if (screenReceiver != null)
      try {
        a.unregisterReceiver(screenReceiver);
      } catch (Exception ignored) {
      }
    if (sensors != null && sensorListener != null) sensors.unregisterListener(sensorListener);
    sensors = manager;
    final int[] period = {20000};
    sensorListener =
        new SensorEventListener() {
          public void onAccuracyChanged(Sensor s, int accuracy) {}

          public void onSensorChanged(SensorEvent e) {
            log(48, "period_us=" + period[0] + " x=" + e.values[0]);
          }
        };
    manager.registerListener(sensorListener, sensor, period[0]);
    screenReceiver =
        new BroadcastReceiver() {
          public void onReceive(Context c, Intent i) {
            boolean off = Intent.ACTION_SCREEN_OFF.equals(i.getAction());
            period[0] = off ? 200000 : 20000;
            manager.unregisterListener(sensorListener);
            manager.registerListener(sensorListener, sensor, period[0]);
            log(48, "screen off=" + off + " sampling_us=" + period[0]);
          }
        };
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SCREEN_OFF);
    filter.addAction(Intent.ACTION_SCREEN_ON);
    a.registerReceiver(screenReceiver, filter);
    trackReceiver(screenReceiver);
    log(48, "register accelerometer 20ms on / 200ms off");
  }

  static void stepCounter(Activity a, int id) {
    sensors = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
    Sensor s = sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    if (s == null) {
      log(id, "No hardware step counter");
      return;
    }
    sensorListener =
        new SensorEventListener() {
          public void onAccuracyChanged(Sensor s, int accuracy) {}

          public void onSensorChanged(SensorEvent event) {
            log(id, "steps=" + event.values[0]);
          }
        };
    sensors.registerListener(sensorListener, s, SensorManager.SENSOR_DELAY_NORMAL);
  }

  static void backup(Activity a) {
    runAsync(
        36,
        () -> {
          try (Cursor c = a.getContentResolver().query(NotesProvider.URI, null, null, null, null)) {
            JSONArrayCompat.save(a, c);
          } catch (Exception e) {
            Log.e("A80_36", "backup", e);
          }
        });
  }

  static void pipe(int id) {
    runAsync(
        id,
        () -> {
          try {
            ParcelFileDescriptor[] pair = ParcelFileDescriptor.createPipe();
            new Thread(
                    () -> {
                      try (OutputStream o =
                          new ParcelFileDescriptor.AutoCloseOutputStream(pair[1])) {
                        o.write("pipe-data".getBytes());
                      } catch (Exception e) {
                        Log.e("A80_pipe", "write", e);
                      }
                    })
                .start();
            try (InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(pair[0])) {
              byte[] b = new byte[32];
              int n = in.read(b);
              log(id, "pipe=" + new String(b, 0, n));
            }
          } catch (Exception e) {
            Log.e("A80_pipe", "failed", e);
          }
        });
  }

  static void alarm(Activity a) {
    PendingIntent fire = alarmIntent(a, ALARM_CODE, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager m = (AlarmManager) a.getSystemService(Context.ALARM_SERVICE);
    m.setAlarmClock(
        new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + 10000, openAppIntent(a)), fire);
  }

  static void toggleComponent(Activity a) {
    ComponentName c = new ComponentName(a, ToggleReceiver.class);
    PackageManager pm = a.getPackageManager();
    int before = pm.getComponentEnabledSetting(c);
    int after =
        before == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    pm.setComponentEnabledSetting(c, after, PackageManager.DONT_KILL_APP);
    log(40, "enabled=" + (after == PackageManager.COMPONENT_ENABLED_STATE_ENABLED));
  }

  static void replay(Activity a) {
    Intent i = new Intent(a, ReplayReceiver.class);
    i.setAction("com.university.app.NONCE");
    i.putExtra("nonce", UUID.randomUUID().toString());
    i.putExtra("time", System.currentTimeMillis());
    a.sendBroadcast(i);
    a.sendBroadcast(i);
  }

  static void release(Activity a) {
    for (int i = 0; i < started.length; i++) started[i] = false;
    jobs.clear();
    taskReceivers.clear();
    taskObservers.clear();
    taskConnections.clear();
    try {
      if (receiver != null) a.unregisterReceiver(receiver);
    } catch (Exception ignored) {
    }
    if (observer != null) a.getContentResolver().unregisterContentObserver(observer);
    if (sensors != null && sensorListener != null) sensors.unregisterListener(sensorListener);
    if (screenReceiver != null)
      try {
        a.unregisterReceiver(screenReceiver);
      } catch (Exception ignored) {
      }
    screenReceiver = null;
    if (bound && connection != null) {
      if (boundRandom != null) boundRandom.unregisterClient();
      a.unbindService(connection);
    }
    boundRandom = null;
    for (BroadcastReceiver r : extraReceivers)
      try {
        a.unregisterReceiver(r);
      } catch (Exception ignored) {
      }
    extraReceivers.clear();
    for (ServiceConnection c : otherConnections)
      try {
        a.unbindService(c);
      } catch (Exception ignored) {
      }
    otherConnections.clear();
    for (ContentObserver o : extraObservers)
      try {
        a.getContentResolver().unregisterContentObserver(o);
      } catch (Exception ignored) {
      }
    extraObservers.clear();
    receiver = null;
    observer = null;
    sensorListener = null;
    bound = false;
  }
}
