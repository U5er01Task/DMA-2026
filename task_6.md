---
title: "Практическая работа: Системные компоненты Android — Services, Broadcast Receivers, Content Providers и их жизненный цикл"
discipline: "Разработка мобильных приложений"
version: "2026.1"
tags:
  [
    android,
    services,
    broadcast-receivers,
    content-providers,
    lifecycle,
    java,
    ipc,
  ]
---

# Практическая работа: Сервисы, широковещательные приемники, поставщики контента и их жизненный цикл

> [!NOTE]
> **Цель работы:** Изучить фундаментальные механизмы фонового выполнения задач, межпроцессного взаимодействия и обмена данными в ОС Android. Освоить проектирование служб (Services), широковещательных приемников (Broadcast Receivers) и поставщиков контента (Content Providers), детально разобрать диаграммы их жизненного цикла и реализовать отказоустойчивую архитектуру взаимодействия компонентов на языке Java.

---

## 📋 Содержание

1. [Архитектура триады компонентов и приоритеты процессов](#1-архитектура-триады-компонентов-и-приоритеты-процессов)
2. [Тема 1. Сервисы (Services): Started, Bound и Foreground](#тема-1-сервисы-services-started-bound-и-foreground)
3. [Тема 2. Жизненный цикл сервисов и флаги перезапуска](#тема-2-жизненный-цикл-сервисов-и-флаги-перезапуска)
4. [Тема 3. Широковещательные приемники (Broadcast Receivers)](#тема-3-широковещательные-приемники-broadcast-receivers)
5. [Тема 4. Жизненный цикл Broadcast Receiver и асинхронность](#тема-4-жизненный-цикл-broadcast-receiver-и-асинхронность)
6. [Тема 5. Поставщики контента (Content Providers) и UriMatcher](#тема-5-поставщики-контента-content-providers-и-urimatcher)
7. [Тема 6. Жизненный цикл и потокобезопасность Content Provider](#тема-6-жизненный-цикл-и-потокобезопасность-content-provider)
8. [50 Смешанных заданий по системным компонентам (Production Scenarios)](#50-смешанных-заданий-по-системным-компонентам)
9. [Критерии оценки и регламент защиты](#критерии-оценки-и-регламент-защиты)

---

## 1. Архитектура триады компонентов и приоритеты процессов

В Android компоненты приложения не имеют собственной функции `main()`. Они активируются системным диспетчером через IPC (Inter-Process Communication) по требованию:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ANDROID OPERATING SYSTEM                        │
│                                                                        │
│   ┌──────────────────┐  Intent  ┌──────────────────────────────────┐   │
│   │    Activities    ├─────────►│        Started / Bound /         │   │
│   │   (UI Screens)   │          │        Foreground Services       │   │
│   └────────▲─────────┘          └─────────────────┬────────────────┘   │
│            │                                      │                    │
│      Local │ Intent                     Intent /  │ System Events      │
│     Events │ Broadcast                   Callback │                    │
│            │                                      │                    │
│   ┌────────┴─────────┐          ┌─────────────────▼────────────────┐   │
│   │    Broadcast     │◄─────────┤        OS System Events          │   │
│   │    Receivers     │  System  │ (BOOT, BATTERY, AIRPLANE, SMS)   │   │
│   └──────────────────┘          └──────────────────────────────────┘   │
│            │                                                           │
│   Resolver │ URI CRUD                                                  │
│   Requests │                                                           │
│   ┌────────▼───────────────────────────────────────────────────────┐   │
│   │                        Content Provider                        │   │
│   │       (SQLite Database, Encrypted Files, Shared App Data)      │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### Иерархия важности процессов (OOM Killer Priority)

При нехватке оперативной памяти демон ядра Android `Low Memory Killer` (LMK) уничтожает процессы, ориентируясь на их оценку важности (`oom_adj_score`):

1. **Foreground Process:** Экран на переднем плане или запущенный `Foreground Service` (минимальный риск уничтожения).
2. **Visible Process:** Процесс, видимый пользователю, но не в фокусе (например, под прозрачным диалоговым окном).
3. **Service Process:** Обычный `Started Service` в фоне (может быть уничтожен при открытии тяжелых 3D-игр).
4. **Cached / Empty Process:** Свернутые неактивные приложения в кэше (уничтожаются в первую очередь).

> [!IMPORTANT]
> Начиная с Android 8.0 (API 26) и вплоть до современных стандартов, операционная система **запрещает** работу невидимых `Started Service` при сворачивании приложения. Любая длительная работа вне экрана обязана быть оформлена либо через `Foreground Service` с постоянным уведомлением, либо через планировщик `WorkManager`.

---

## Тема 1. Сервисы (Services): Started, Bound и Foreground

Сервис — это компонент без пользовательского интерфейса, выполняющий длительные операции в фоновом режиме.

### Архитектурные разновидности сервисов

| Тип сервиса            | Метод запуска              | Жизненный цикл                                                         | Типичный сценарий                       |
| :--------------------- | :------------------------- | :--------------------------------------------------------------------- | :-------------------------------------- |
| **Started Service**    | `startService()`           | Живет, пока сам не вызовет `stopSelf()` или не вызовут `stopService()` | Одноразовая фоновая выгрузка логов      |
| **Bound Service**      | `bindService()`            | Живет, пока к нему привязан хотя бы один активный клиент               | Управление локальным аудиоплеером из UI |
| **Foreground Service** | `startForegroundService()` | Обязан показать несмахиваемое уведомление в течение 5 секунд           | Непрерывный GPS-трекер курьера, шагомер |

> [!WARNING]
> Сервис по умолчанию выполняется в **главном потоке (Main / UI Thread)** своего процесса! Длительные математические расчеты, сетевые вызовы и дисковый ввод-вывод внутри сервиса вызывают ошибку ANR (Application Not Responding). Для тяжелых задач внутри сервиса создается отдельный поток (`Thread`, `ExecutorService` или корутина).

### Пример кода (Современный Foreground Service с типом)

#### Декларация в AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application ...>
        <service
            android:name=".services.LocationTrackerService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="location" />
    </application>
</manifest>
```

#### Исходный код сервиса (LocationTrackerService.java)

```java
package com.university.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class LocationTrackerService extends Service {

    public static final String CHANNEL_ID = "tracker_service_channel";
    public static final int NOTIFICATION_ID = 101;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Трекер курьера активен")
                .setContentText("Координаты передаются диспетчеру в реальном времени...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        // Запуск переднего плана со строгим типом (Android 14+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        // Запуск полезной нагрузки в отдельном потоке
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Started Service не привязывается к UI
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Служба отслеживания",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
```

### Задания для закрепления

1. **Служба таймера обратного отсчета:** Создайте Started Service, который считает секунды от 60 до 0 в фоновом потоке и логирует значение в Logcat.
2. **Foreground-сервис аудиоплеера:** Реализуйте Foreground Service с типом `mediaPlayback`, показывающий постоянное уведомление с названием играющей мелодии.
3. **Безопасная остановка (stopSelf):** Доработайте службу скачивания файла так, чтобы после завершения цикла передачи байт она вызывала `stopSelf(startId)`, гарантируя, что не прервутся новые поступившие запросы.
4. **Служба с отдельным процессом:** Задекларируйте сервис в манифесте с атрибутом `android:process=":downloader"` и выведите имя процесса через метод `Process.myPid()`.
5. **Проверка разрешений перед стартом:** Напишите метод в Activity, проверяющий наличие системного разрешения `POST_NOTIFICATIONS` перед вызовом `startForegroundService()`.

---

## Тема 2. Жизненный цикл сервисов и флаги перезапуска

### Диаграмма жизненного цикла Services

```
        STARTED SERVICE                          BOUND SERVICE
               │                                       │
               ▼                                       ▼
        ┌──────────────┐                        ┌──────────────┐
        │  onCreate()  │                        │  onCreate()  │
        └──────┬───────┘                        └──────┬───────┘
               │                                       │
               ▼                                       ▼
      ┌──────────────────┐                       ┌───────────┐
      │ onStartCommand() │                       │ onBind()  │
      └────────┬─────────┘                       └─────┬─────┘
               │                                       │
               ▼                                       ▼
       [ Служба активна ]                      [ Клиенты привязаны ]
               │                                       │
    stopSelf() │ stopService()           Все отвязались│ unbindService()
               ▼                                       ▼
               │                                 ┌───────────┐
               │                                 │ onUnbind()│
               │                                 └─────┬─────┘
               │                                       │
               └───────────────────┬───────────────────┘
                                   │
                                   ▼
                            ┌───────────────┐
                            │  onDestroy()  │
                            └───────────────┘
                                   │
                                   ▼
                           [ Сервис завершен ]
```

### Флаги возврата из метода onStartCommand()

Поведение системы при аварийном уничтожении процесса определяется возвращаемым значением:

- `START_STICKY`: Система пересоздает сервис после освобождения памяти, вызывает `onCreate() -> onStartCommand()`, но передает в `Intent` значение `null` (если не было новых запросов). Идеально для фоновых слушателей сокетов и плееров.
- `START_NOT_STICKY`: Система **не** пересоздает службу автоматически. Она запустится заново только при явном повторном вызове `startService()`. Идеально для разовых действий (отправка аналитики).
- `START_REDELIVER_INTENT`: Система гарантированно пересоздает сервис и повторно доставляет **тот же самый** объект `Intent` с его аргументами. Критично для загрузки важных файлов и финансовых транзакций.

### Реализация Bound Service (Привязанная служба)

```java
public class StepCounterService extends Service {
    private final IBinder binder = new LocalBinder();
    private int stepCount = 0;

    public class LocalBinder extends Binder {
        public StepCounterService getService() {
            return StepCounterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public int getSteps() {
        return stepCount++;
    }
}
```

Подключение из Activity через интерфейс `ServiceConnection`:

```java
private StepCounterService myService;
private boolean isBound = false;

private final ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        StepCounterService.LocalBinder binder = (StepCounterService.LocalBinder) service;
        myService = binder.getService();
        isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        isBound = false;
    }
};

// Привязка: bindService(intent, connection, Context.BIND_AUTO_CREATE);
// Отвязка: if (isBound) { unbindService(connection); isBound = false; }
```

### Задания для закрепления

1. **Служба генерации случайных чисел (Bound):** Создайте привязанный сервис, предоставляющий метод `getRandomNumber()`. В Activity отображайте полученное число в `TextView`.
2. **Эксперимент с START_STICKY:** Напишите службу со счетчиком секунд, принудительно «убейте» процесс через вкладку Logcat или команду `adb shell am kill` и зафиксируйте в логах перезапуск без интента.
3. **Управление жизненным циклом через onUnbind:** Переопределите метод `boolean onUnbind(Intent intent)`. Настройте возвращаемое значение `true`, чтобы при повторном подключении клиентов вызывался `onRebind()`.
4. **Слушатель множественных клиентов:** Реализуйте в Bound Service счетчик одновременно подключенных экранов (клиентов) и выводите их количество при каждом `bind/unbind`.
5. **Комбинированный сервис (Started + Bound):** Создайте службу, которая сначала запускается через `startService()` (чтобы не умирать при выходе из экрана), а затем временно привязывается через `bindService()` для чтения статуса.

---

## Тема 3. Широковещательные приемники (Broadcast Receivers)

`BroadcastReceiver` — это системный почтовый ящик, предназначенный для приема и обработки широковещательных сообщений (Intents) от операционной системы или других приложений.

### Два способа регистрации приемника

| Параметр сравнения          | Статическая регистрация                       | Динамическая регистрация                     |
| :-------------------------- | :-------------------------------------------- | :------------------------------------------- |
| **Где объявляется**         | В файле `AndroidManifest.xml`                 | В Java-коде (`registerReceiver()`)           |
| **Активность**              | Доступен всегда, даже если приложение закрыто | Работает только пока жив экран/контекст      |
| **Современные ограничения** | Заблокировано большинство неявных интентов    | Разрешены любые интенты                      |
| **Управление памятью**      | Система вынуждена пробуждать весь процесс     | Освобождается вызовом `unregisterReceiver()` |

### Безопасность в Android 13–15+: Флаги экспорта

При регистрации динамического приемника разработчик **обязан** указать флаг доступности для внешних приложений:

- `Context.RECEIVER_EXPORTED`: Приемник доступен для сообщений от любых сторонних приложений.
- `Context.RECEIVER_NOT_EXPORTED`: Приемник защищен и реагирует только на события внутри собственного приложения или системные события ОС.

### Пример кода динамической регистрации

```java
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {
                Toast.makeText(context, "Связь потеряна! Переход в офлайн.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Сеть восстановлена!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
```

Регистрация и отписка в жизненном цикле Activity:

```java
public class MainActivity extends AppCompatActivity {
    private NetworkChangeReceiver receiver;

    @Override
    protected void onStart() {
        super.onStart();
        receiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        // Обязательный флаг безопасности Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}
```

### Задания для закрепления

1. **Приемник уровня заряда батареи:** Зарегистрируйте динамический приемник для системного действия `Intent.ACTION_BATTERY_LOW` и выведите предупреждение на экран.
2. **Перехват режима полета:** Напишите приемник события `Intent.ACTION_AIRPLANE_MODE_CHANGED` и считайте состояние переключателя.
3. **Статический приемник перезагрузки:** Настройте в манифесте ресивер с фильтром `BOOT_COMPLETED`, требующий разрешения `android.permission.RECEIVE_BOOT_COMPLETED`.
4. **Кастомный внутрипрограммный бродкаст:** Отправьте кастомное широковещательное намерение `com.university.CUSTOM_ALERT` через `sendBroadcast()` и примите его во втором ресивере.
5. **Упорядоченный бродкаст (Ordered Broadcast):** Напишите два приемника с разными приоритетами (`android:priority="100"` и `"200"`), продемонстрировав перехват и прерывание цепочки методом `abortBroadcast()`.

---

## Тема 4. Жизненный цикл Broadcast Receiver и асинхронность

У `BroadcastReceiver` самый короткий жизненный цикл среди всех базовых компонентов Android.

### Схема жизненного цикла

```
       Система рассылает Intent (Бродкаст)
                       │
                       ▼
          ┌─────────────────────────┐
          │  Инстанцирование нового  │
          │  объекта класса         │
          └────────────┬────────────┘
                       │
                       ▼
          ┌─────────────────────────┐
          │       onReceive()       │ <── Максимальный лимит: 10 секунд!
          └────────────┬────────────┘
                       │
                       ▼
          ┌─────────────────────────┐
          │  Объект уничтожается    │
          │  и утилизируется GC     │
          └─────────────────────────┘
```

> [!CRITICAL]
> Метод `onReceive()` выполняется строго в **главном потоке**. Лимит времени выполнения составляет всего **10 секунд**, после чего операционная система принудительно аварийно завершает приложение с диалоговым окном ANR.
> Внутри `onReceive()` **категорически запрещено** запускать асинхронные операции в обычных потоках (`new Thread().start()`), так как после выхода из метода процесс признается фоновым и мгновенно уничтожается системой.

### Механизм goAsync() для продления жизни

Если в приемнике необходимо выполнить небольшую асинхронную операцию (до 30–60 секунд):

```java
public class AsyncOrderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();

        new Thread(() -> {
            try {
                // Выполнение быстрой дисковой или локальной операции
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            } finally {
                // Обязательный сигнал ОС о завершении обработки
                pendingResult.finish();
            }
        }).start();
    }
}
```

### Задания для закрепления

1. **Проверка главного потока:** Выведите идентификатор текущего потока в `onReceive()` через `Looper.myLooper() == Looper.getMainLooper()` и убедитесь, что код выполняется на UI-потоке.
2. **Имитация ошибки ANR:** Создайте в учебном проекте задержку `Thread.sleep(15000)` в `onReceive()` и зафиксируйте аварийное поведение системы в логах.
3. **Безопасная передача задачи в WorkManager:** Напишите в `onReceive()` постановку тяжелой фоновой задачи в очередь через `WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)`.
4. **Асинхронная запись в SharedPreferences:** Используя механизм `goAsync()`, сохраните в локальные настройки время наступления системного события перезагрузки.
5. **Предотвращение утечки памяти:** Покажите на практике, почему отсутствие вызова `unregisterReceiver()` в методе `onDestroy()` или `onStop()` Activity приводит к утечке контекста окна.

---

## Тема 5. Поставщики контента (Content Providers) и UriMatcher

`ContentProvider` инкапсулирует данные и предоставляет стандартный унифицированный интерфейс межпроцессного доступа (CRUD) для внешних или внутренних клиентов через протокол адресации `content://`.

### Анатомия Content URI

```
 content://com.university.app.provider/students/42
 └─┬────┘  └─────────────┬───────────┘ └───┬────┘ └┬┘
   │                     │                 │       │
 Схема               Authority            Путь    Идентификатор
(Стандартная)       (Уникальный ID      (Таблица)  (Строка в БД)
                     провайдера)
```

### Шесть обязательных методов класса ContentProvider

1. `boolean onCreate()` — инициализация провайдера (запускается автоматически при старте ОС).
2. `Cursor query(...)` — выборка данных (аналог `SELECT`).
3. `Uri insert(...)` — добавление новой записи (аналог `INSERT`).
4. `int update(...)` — обновление существующих данных (аналог `UPDATE`).
5. `int delete(...)` — удаление строк (аналог `DELETE`).
6. `String getType(...)` — возврат MIME-типа данных по указанному URI.

### Пример кода (Поставщик заметок с UriMatcher)

```java
public class NotesProvider extends ContentProvider {

    public static final String AUTHORITY = "com.university.app.provider.notes";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notes");

    private static final int CODE_NOTES_DIR = 1;
    private static final int CODE_NOTE_ITEM = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "notes", CODE_NOTES_DIR);
        uriMatcher.addURI(AUTHORITY, "notes/#", CODE_NOTE_ITEM);
    }

    @Override
    public boolean onCreate() {
        // Инициализация локальной базы данных SQLite / Room
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case CODE_NOTES_DIR:
                // Выборка всей таблицы
                break;
            case CODE_NOTE_ITEM:
                // Извлечение конкретной записи по ID: ContentUris.parseId(uri)
                break;
            default:
                throw new IllegalArgumentException("Неизвестный URI: " + uri);
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return switch (uriMatcher.match(uri)) {
            case CODE_NOTES_DIR -> "vnd.android.cursor.dir/vnd.com.university.note";
            case CODE_NOTE_ITEM -> "vnd.android.cursor.item/vnd.com.university.note";
            default -> throw new IllegalArgumentException("Неизвестный URI: " + uri);
        };
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Вставка в БД и уведомление наблюдателей
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(CONTENT_URI, 1);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
```

Декларация в `AndroidManifest.xml`:

```xml
<provider
    android:name=".providers.NotesProvider"
    android:authorities="com.university.app.provider.notes"
    android:exported="true"
    android:readPermission="com.university.permission.READ_NOTES" />
```

### Задания для закрепления

1. **Чтение системных контактов:** С помощью `ContentResolver.query()` обратитесь к URI `ContactsContract.CommonDataKinds.Phone.CONTENT_URI` и выведите список имен в лог.
2. **Медиатека изображений (MediaStore):** Напишите запрос через ContentResolver к `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` для получения названий и размеров фото.
3. **Реализация UriMatcher с маской строки:** Добавьте в `UriMatcher` поддержку строкового фильтра категорий с маской `*` (`notes/category/*`).
4. **Валидация входящих значений в insert:** Реализуйте в методе `insert()` проверку на наличие обязательного текстового поля и выбрасывайте `IllegalArgumentException` при его отсутствии.
5. **Уведомление об изменениях данных:** Реализуйте регистрацию `ContentObserver` в Activity, чтобы экран автоматически обновлялся при вызове `notifyChange()` провайдером.

---

## Тема 6. Жизненный цикл и потокобезопасность Content Provider

### Особенности инициализации

Метод `ContentProvider.onCreate()` вызывается операционной системой на **главном потоке** еще **до** вызова метода `Application.onCreate()` самого приложения!

```
                    ЗАПУСК ПРОЦЕССА ПРИЛОЖЕНИЯ
                                │
                                ▼
                    ┌────────────────────────┐
                    │  Application создается │
                    └───────────┬────────────┘
                                │
                                ▼
                    ┌────────────────────────┐
                    │ ContentProvider        │ <── Выполняется РАНЬШЕ,
                    │ .onCreate()            │     чем Application.onCreate()!
                    └───────────┬────────────┘
                                │
                                ▼
                    ┌────────────────────────┐
                    │ Application            │
                    │ .onCreate()            │
                    └───────────┬────────────┘
                                │
                                ▼
                    ┌────────────────────────┐
                    │ Activity               │
                    │ .onCreate()            │
                    └────────────────────────┘
```

> [!WARNING]
> Если в `ContentProvider.onCreate()` выполнять тяжелые блокирующие операции (загрузка файлов из сети, тяжелые миграции больших баз данных), запуск всего приложения замедлится, провоцируя зависание системы при холодном старте (Cold Start).

### Потокобезопасность (Thread Safety)

В отличие от `onCreate()`, методы `query()`, `insert()`, `update()` и `delete()` вызываются из пула системных потоков IPC (Binder Thread Pool). Это означает, что:

- Провайдер может одновременно обслуживать запросы от десятков разных приложений.
- Все методы CRUD **обязаны быть строго потокобезопасными** (`synchronized`, транзакции базы данных SQLite, потокобезопасные коллекции).

### Задания для закрепления

1. **Анализ порядка инициализации:** Поставьте контрольные логи в `Application.onCreate()`, `ContentProvider.onCreate()` и `MainActivity.onCreate()` и сравните их таймстемпы в Logcat.
2. **Синхронизация методов CRUD:** Оберните критические секции вставки в блок `synchronized` и объясните риски блокировки пула потоков Binder.
3. **Использование транзакций SQLite:** Напишите метод пакетной вставки `applyBatch()` с открытием транзакции через `db.beginTransaction()` и `db.setTransactionSuccessful()`.
4. **Настройка FileProvider:** Сконфигурируйте системный `androidx.core.content.FileProvider` с файлом путей `file_paths.xml` для безопасной выдачи URI локального PDF-файла.
5. **Защита провайдера от SQL-инъекций:** Продемонстрируйте подстановку параметров через аргументы `selectionArgs` вместо конкатенации сырых строк.

---

## 50 Смешанных заданий по системным компонентам

Задания охватывают реальные кейсы промышленной мобильной разработки и системной интеграции.

### Блок 1: Фоновые службы, медиа и геотрекинг (Services)

1. **Фоновый шагомер (Step Tracker):** Разработайте сервис переднего плана, слушающий аппаратный датчик `TYPE_STEP_COUNTER` и обновляющий число шагов в уведомлении раз в 10 секунд.
2. **Аудиоплеер с управлением из шторки:** Реализуйте привязанный сервис с кнопками «Play», «Pause», «Next» в интерактивном уведомлении (`NotificationCompat.Action`).
3. **Сервис пакетной выгрузки фотографий:** Напишите службу скачивания архива из 100 файлов с флагом `START_REDELIVER_INTENT` и отображением прогресс-бара в процентах.
4. **Фоновый веб-сокет клиент:** Создайте сервис с непрерывным WebSocket-соединением для получения биржевых котировок, автоматически восстанавливающий связь при падении.
5. **Сервис геозоны (Geofencing Monitor):** Разработайте сервис, непрерывно запрашивающий GPS-координаты и подающий звуковой сигнал при выходе из радиуса 500 метров от склада.
6. **Служба сжатия видеофайлов:** Напишите сервис с очередью задач на перекодирование видеороликов с использованием фонового пула `Executors.newSingleThreadExecutor()`.
7. **IPC через Messenger:** Создайте службу, взаимодействующую с клиентами из другого процесса с помощью объектов `Messenger` и сообщений `Message`.
8. **Служба очистки кэша по расписанию:** Реализуйте сервис, подсчитывающий размер каталога кэша и удаляющий файлы старше 7 дней при старте.
9. **Служба мониторинга буфера обмена:** Разработайте сервис, отслеживающий появление в буфере обмена телефонных номеров и предлагающий быстрый звонок.
10. **Ограничитель времени работы сервиса:** Реализуйте авто-остановку Foreground-службы через 30 минут работы с помощью таймера и метода `stopForeground(true)`.

### Блок 2: Системные события и широковещательные приемники (Broadcasts)

11. **Детектор подключения зарядного устройства:** Зарегистрируйте приемник событий `ACTION_POWER_CONNECTED` и `ACTION_POWER_DISCONNECTED` с выводом текущего типа зарядки (быстрая, беспроводная, USB).
12. **Монитор изменения часового пояса:** Напишите приемник `ACTION_TIMEZONE_CHANGED`, пересчитывающий время локальных будильников при перелете пользователя.
13. **Перехватчик исходящих SMS (Служебный режим):** Сконфигурируйте ресивер для обработки событий телеком-модуля при наличии соответствующих прав.
14. **Сигнализатор переполнения памяти накопителя:** Напишите приемник системного действия `ACTION_DEVICE_STORAGE_LOW`, автоматически инициирующий сброс временных картинок.
15. **Автоматический запуск службы после перезагрузки:** Настройте цепочку: прием события `BOOT_COMPLETED` -> валидация настроек -> вызов `startForegroundService()`.
16. **Перехват подключения гарнитуры:** Напишите приемник события `ACTION_HEADSET_PLUG`, автоматически ставящий аудиоплеер на паузу при извлечении наушников.
17. **Локальный шифратор широковещательных сообщений:** Реализуйте отправку сообщений, подписанных цифровой подписью, с проверкой в ресивере через кастомное разрешение уровня `signature`.
18. **Индикатор изменения системного языка:** Сконфигурируйте ресивер действия `ACTION_LOCALE_CHANGED` для мгновенной переинициализации локализованных кэшей.
19. **Динамический фильтр бродкастов с приоритетом:** Создайте 3 приемника в разных модулях и продемонстрируйте передачу модифицированных результатов через `setResultData()`.
20. **Антикраш-ресивер с таймаутом:** Оберните логику `onReceive()` в контролируемый асинхронный контекст `goAsync()` с принудительным завершением через 8 секунд.

### Блок 3: Обмен данными и файлами (Content Providers)

21. **Провайдер безопасного доступа к логам:** Спроектируйте `ContentProvider` с правами только на чтение (`readPermission`), отдающий зашифрованные логи приложения аналитическому сервису.
22. **Интеграция с системным поиском (Quick Search Box):** Реализуйте провайдер, возвращающий курсор подсказок по схеме `SearchManager.SUGGEST_URI_PATH_QUERY`.
23. **Экспорт визитных карточек (vCard):** Создайте провайдер, отдающий файлы контактов в формате `.vcf` через метод `openFile()`.
24. **Синхронизация корзины между двумя приложениями:** Разработайте два разных приложения одной компании, где второе читает состав корзины первого через `ContentResolver`.
25. **Провайдер конфигураций приложения:** Реализуйте шифрованный поставщик настроек, хранящий пары «ключ-значение» в защищенном хранилище EncryptedSharedPreferences.
26. **Передача снимка с камеры через FileProvider:** Настройте генерацию безопасного `content://` URI для сохранения полноразмерного фото стандартным приложением камеры.
27. **Пакетная вставка 10 000 строк:** Оптимизируйте метод `bulkInsert()` в кастомном провайдере для быстрой загрузки каталога товаров в базу данных.
28. **Провайдер с поддержкой MIME-типов PDF и CSV:** Реализуйте метод `getStreamTypes()` для отдачи отчетов в нескольких форматах по выбору клиента.
29. **Мониторинг базы данных через ContentObserver:** Напишите сервис, слушающий изменения в системном журнале звонков и ведущий статистику вызовов.
30. **Полнотекстовый поиск по заметкам:** Спроектируйте ContentProvider над таблицей виртуальной полнотекстовой базы SQLite FTS4.

### Блок 4: Архитектурные комбинации и межкомпонентная интеграция

31. **Комплекс «Фоновый загрузчик»:** Activity запускает Service скачивания -> Service по окончании рассылает Broadcast -> Receiver обновляет UI или показывает пуш.
32. **Комплекс «Аварийный сброс»:** Ресивер низкого заряда батареи останавливает тяжелые фоновые службы синхронизации и переводит провайдер данных в режим энергосбережения.
33. **Комплекс «Кэширующий прокси»:** ContentProvider при отсутствии данных в локальной БД обращается к Service для фонового запроса в сеть и уведомляет UI через `notifyChange()`.
34. **Удаленное управление сервисом через Broadcast:** Реализуйте управление плеером (Play/Stop/Mute) путем отправки внешних интентов из тестового консольного скрипта `adb shell am broadcast`.
35. **Двусторонняя связь Activity и Bound Service:** Настройте паттерн Callback/Listener, чтобы служба сама уведомляла экран о появлении новых данных датчиков.
36. **Автоматический бэкап базы в файл:** По сигналу системного времени (`ACTION_TIME_TICK`) сервис считывает данные из ContentProvider и сохраняет их в JSON-архив.
37. **Контроль доступа к компонентам по белому списку пакетов:** Проверяйте имя вызывающего пакета через `Binder.getCallingUid()` и `PackageManager.getNameForUid()`.
38. **Передача дескрипторов файлов (PipeDataWriter):** Реализуйте передачу потоковых данных из сервиса в провайдер без сохранения промежуточного файла на диск через канал `ParcelFileDescriptor.createPipe()`.
39. **Имитация системного будильника:** Соберите связку из `AlarmManager`, BroadcastReceiver и высокоприоритетного Foreground Service для пробуждения экрана.
40. **Динамическое отключение компонентов:** Реализуйте включение и выключение BroadcastReceiver во время работы через метод `PackageManager.setComponentEnabledSetting()`.

### Блок 5: Безопасность, оптимизация и отказоустойчивость

41. **Защита от атак повторного воспроизведения (Broadcast Replay Attack):** Добавьте проверку таймстемпа и случайной одноразовой соли (Nonce) в широковещательные интенты.
42. **Изоляция провайдера от обхода путей (Path Traversal):** Напишите валидатор путей в методе `openFile()`, блокирующий попытки извлечения файлов через последовательности `../`.
43. **Ограничение прав на чтение и запись:** Настройте в манифесте для провайдера раздельные атрибуты `android:readPermission` и `android:writePermission`.
44. **Профилирование утечек памяти в сервисах:** Проанализируйте длительно работающий сервис на предмет удержания ссылок на уничтоженные Activity через статические поля.
45. **Адаптация сервисов под режим глубокого сна (Doze Mode):** Реализуйте корректную обработку перехода устройства в спящий режим и использование `PowerManager.WakeLock` с обязательным таймаутом.
46. **Защита от неконтролируемого запуска службы:** Сделайте сервис недоступным для внешних приложений (`android:exported="false"`), защитив его от Intent Injection.
47. **Отказоустойчивость при падении SQLite:** Оберните все операции провайдера контента в обработку `SQLiteDatabaseCorruptException` с аварийным пересозданием файла базы данных.
48. **Контроль энергопотребления датчиков в сервисе:** Реализуйте снижение частоты опроса акселерометра в 10 раз при выключенном дисплее (`ACTION_SCREEN_OFF`).
49. **Безопасная выдача временных разрешений на URI:** Реализуйте генерацию интента с флагами `FLAG_GRANT_READ_URI_PERMISSION` для передачи фотографии стороннему редактору.
50. **Комплексная архитектура Enterprise-модуля:**
    - Разработайте защищенный Foreground Service с типом `dataSync`.
    - Подключите локальный ContentProvider с постраничной пагинацией.
    - Настройте динамический BroadcastReceiver для контроля сетевого статуса.
    - Обеспечьте нулевую вероятность возникновения ANR и полную очистку системных ресурсов в методах `onDestroy()`.

---
