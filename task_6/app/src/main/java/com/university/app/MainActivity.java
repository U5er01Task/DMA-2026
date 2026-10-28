package com.university.app;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

public final class MainActivity extends Activity {
  private LinearLayout panel;
  private TextView status;
  private int pending = -1;
  private boolean downloadRegistered;
  private final BroadcastReceiver downloaded =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
          int count = i.getIntExtra("count", 0);
          status.setText("Скачано файлов: " + count + " / 100");
          android.util.Log.i("A80_31", "Activity updated after Broadcast count=" + count);
        }
      };

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    Log.i("A80_6_1", "Activity.onCreate time=" + System.nanoTime());
    ScrollView s = new ScrollView(this);
    panel = new LinearLayout(this);
    panel.setOrientation(1);
    panel.setPadding(16, 12, 16, 12);
    s.addView(panel);
    setContentView(s);
    status = new TextView(this);
    status.setTextSize(18);
    status.setText("80 заданий / Java / com.university.app");
    panel.addView(status);
    for (int i = 0; i < 80; i++) {
      final int n = i;
      Button b = new Button(this);
      b.setAllCaps(false);
      b.setText((i < 30 ? (i / 5 + 1) + "." + (i % 5 + 1) : "" + (i - 29)) + " — " + LABELS[i]);
      LinearLayout row = new LinearLayout(this);
      row.setOrientation(LinearLayout.HORIZONTAL);
      row.addView(b, new LinearLayout.LayoutParams(0, -2, 1));
      Button stop = new Button(this);
      stop.setAllCaps(false);
      stop.setText("■ Стоп");
      row.addView(stop);
      panel.addView(row);
      b.setOnClickListener(v -> execute(n));
      stop.setOnClickListener(
          v -> {
            if (pending == n) pending = -1;
            try {
              TaskActions.cancel(this, n);
              status.setText("Задание " + (n + 1) + ": остановлено / сброшено");
            } catch (Exception e) {
              Log.e("A80_" + (n + 1), "Stop failed", e);
              status.setText("Ошибка при остановке: " + e);
            }
          });
    }
    int target = getIntent().getIntExtra("task", -1);
    if (target >= 0 && target < 80)
      new Handler(Looper.getMainLooper()).postDelayed(() -> execute(target), 400);
  }

  private void execute(int n) {
    try {
      TaskActions.markStarted(n);
      TaskActions.perform(this, n, status);
    } catch (SecurityException e) {
      Log.e("A80_" + (n + 1), "Permission missing", e);
      status.setText("Разрешение: " + e.getMessage());
    } catch (Exception e) {
      Log.e("A80_" + (n + 1), "Failed", e);
      status.setText("Ошибка: " + e);
    } finally {
      TaskActions.clearCurrentTask();
    }
  }

  void requestFor(int number, String permission) {
    pending = number;
    if (Build.VERSION.SDK_INT >= 23) requestPermissions(new String[] {permission}, 42);
  }

  @Override
  protected void onActivityResult(int request, int result, Intent data) {
    super.onActivityResult(request, result, data);
    if (request == 106 && result == RESULT_OK && data != null && data.getData() != null) {
      android.net.Uri uri = data.getData();
      try {
        getContentResolver()
            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      } catch (Exception ignored) {
      }
      Intent i = new Intent(this, VideoTranscodeService.class).setData(uri);
      if (TaskActions.isStarted(35))
        androidx.core.content.ContextCompat.startForegroundService(this, i);
    }
  }

  @Override
  public void onRequestPermissionsResult(int code, String[] p, int[] g) {
    super.onRequestPermissionsResult(code, p, g);
    if (code == 42 && pending >= 0) {
      int task = pending;
      pending = -1;
      if (g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) execute(task);
      else status.setText("Разрешение не предоставлено");
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    IntentFilter filter = new IntentFilter(DownloadForegroundService.ACTION_FINISHED);
    if (Build.VERSION.SDK_INT >= 33)
      registerReceiver(downloaded, filter, Context.RECEIVER_NOT_EXPORTED);
    else registerReceiver(downloaded, filter);
    downloadRegistered = true;
  }

  @Override
  protected void onStop() {
    if (downloadRegistered) {
      unregisterReceiver(downloaded);
      downloadRegistered = false;
    }
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    TaskActions.release(this);
    super.onDestroy();
  }

  static final String[] LABELS =
      new String[] {
        "Таймер 60→0",
        "Foreground-аудиоплеер",
        "Безопасный stopSelf",
        "Отдельный процесс",
        "Разрешение POST_NOTIFICATIONS",
        "Bound генератор чисел",
        "START_STICKY",
        "onUnbind/onRebind",
        "Несколько клиентов",
        "Started + Bound",
        "Низкий заряд",
        "Режим полёта",
        "BOOT_COMPLETED",
        "Своя рассылка",
        "Ordered Broadcast",
        "Главный поток Receiver",
        "ANR 15 секунд",
        "Поставить Worker",
        "goAsync + Preferences",
        "Регистрация и отписка",
        "Системные контакты",
        "MediaStore фото",
        "UriMatcher category/*",
        "Валидация insert",
        "ContentObserver",
        "Порядок инициализации",
        "Синхронизация CRUD",
        "Транзакция applyBatch",
        "FileProvider PDF",
        "SQL selectionArgs",
        "Фоновый шагомер (Step Tracker)",
        "Аудиоплеер с управлением из шторки",
        "Сервис пакетной выгрузки фотографий",
        "Фоновый веб-сокет клиент",
        "Сервис геозоны (Geofencing Monitor)",
        "Служба сжатия видеофайлов",
        "IPC через Messenger",
        "Служба очистки кэша по расписанию",
        "Служба мониторинга буфера обмена",
        "Ограничитель времени работы сервиса",
        "Детектор подключения зарядного устройства",
        "Монитор изменения часового пояса",
        "Перехватчик исходящих SMS (Служебный режим)",
        "Сигнализатор переполнения памяти накопителя",
        "Автоматический запуск службы после перезагрузки",
        "Перехват подключения гарнитуры",
        "Локальный шифратор широковещательных сообщений",
        "Индикатор изменения системного языка",
        "Динамический фильтр бродкастов с приоритетом",
        "Антикраш-ресивер с таймаутом",
        "Провайдер безопасного доступа к логам",
        "Интеграция с системным поиском (Quick Search Box)",
        "Экспорт визитных карточек (vCard)",
        "Синхронизация корзины между двумя приложениями",
        "Провайдер конфигураций приложения",
        "Передача снимка с камеры через FileProvider",
        "Пакетная вставка 10 000 строк",
        "Провайдер с поддержкой MIME-типов PDF и CSV",
        "Мониторинг базы данных через ContentObserver",
        "Полнотекстовый поиск по заметкам",
        "Комплекс «Фоновый загрузчик»",
        "Комплекс «Аварийный сброс»",
        "Комплекс «Кэширующий прокси»",
        "Удаленное управление сервисом через Broadcast",
        "Двусторонняя связь Activity и Bound Service",
        "Автоматический бэкап базы в файл",
        "Контроль доступа к компонентам по белому списку пакетов",
        "Передача дескрипторов файлов (PipeDataWriter)",
        "Имитация системного будильника",
        "Динамическое отключение компонентов",
        "Защита от атак повторного воспроизведения (Broadcast Replay Attack)",
        "Изоляция провайдера от обхода путей (Path Traversal)",
        "Ограничение прав на чтение и запись",
        "Профилирование утечек памяти в сервисах",
        "Адаптация сервисов под режим глубокого сна (Doze Mode)",
        "Защита от неконтролируемого запуска службы",
        "Отказоустойчивость при падении SQLite",
        "Контроль энергопотребления датчиков в сервисе",
        "Безопасная выдача временных разрешений на URI",
        "Комплексная архитектура Enterprise-модуля"
      };
}
