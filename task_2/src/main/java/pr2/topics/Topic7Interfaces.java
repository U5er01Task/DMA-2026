package pr2.topics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Тема 7. Абстрактные классы и интерфейсы.
 */
public class Topic7Interfaces {

    // 1. Хранилище ключ-значение
    public interface KeyValueStorage {
        void save(String key, String value);

        String get(String key);

        void clear();
    }

    public static class MemoryStorage implements KeyValueStorage {
        private final Map<String, String> data = new HashMap<>();

        @Override
        public void save(String key, String value) { data.put(key, value); }

        @Override
        public String get(String key) { return data.get(key); }

        @Override
        public void clear() { data.clear(); }
    }

    // 2. Колбэк загрузки изображения
    public interface ImageLoadCallback {
        void onSuccess(String bitmapRef);

        void onError(Throwable error);
    }

    public static void loadImage(String url, ImageLoadCallback callback) {
        if (url.startsWith("https://")) {
            callback.onSuccess("bitmap@" + Math.abs(url.hashCode()));
        } else {
            callback.onError(new IllegalArgumentException("Разрешены только https-ссылки: " + url));
        }
    }

    // 3. Слушатель фоновой задачи с default-методом
    public interface BackgroundTaskListener {
        default void onProgress(int percentage) {
            System.out.println("   прогресс: " + percentage + "%");
        }

        void onComplete();
    }

    public static void runBackgroundTask(BackgroundTaskListener listener) {
        for (int p = 0; p <= 100; p += 25) {
            listener.onProgress(p);
        }
        listener.onComplete();
    }

    // 4. Класс, реализующий два интерфейса
    public interface Playable {
        void play();

        void stop();
    }

    public interface Shareable {
        void shareViaBluetooth();
    }

    public static class MediaFile implements Playable, Shareable {
        private final String fileName;
        private boolean playing;

        public MediaFile(String fileName) { this.fileName = fileName; }

        @Override
        public void play() {
            playing = true;
            System.out.println("   > воспроизведение " + fileName);
        }

        @Override
        public void stop() {
            if (playing) {
                playing = false;
                System.out.println("   [стоп] остановлено " + fileName);
            }
        }

        @Override
        public void shareViaBluetooth() {
            System.out.println("   отправка " + fileName + " по Bluetooth...");
        }
    }

    // 5. Функциональный интерфейс
    @FunctionalInterface
    public interface PredicateValidator<T> {
        boolean validate(T data);
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 7 ===");

        KeyValueStorage storage = new MemoryStorage();
        storage.save("token", "abc123");
        storage.save("theme", "dark");
        System.out.println("1) theme = " + storage.get("theme") + ", token = " + storage.get("token"));
        storage.clear();
        System.out.println("   после clear(): token = " + storage.get("token"));

        ImageLoadCallback callback = new ImageLoadCallback() {
            @Override
            public void onSuccess(String bitmapRef) {
                System.out.println("   загружено: " + bitmapRef);
            }

            @Override
            public void onError(Throwable error) {
                System.out.println("   ошибка: " + error.getMessage());
            }
        };
        System.out.println("2) Загрузка картинок:");
        loadImage("https://cdn.example.com/avatar.jpg", callback);
        loadImage("http://insecure.example.com/a.jpg", callback);

        System.out.println("3) Фоновая задача:");
        runBackgroundTask(() -> System.out.println("   задача завершена"));

        System.out.println("4) MediaFile:");
        MediaFile track = new MediaFile("voice_note.m4a");
        track.play();
        track.stop();
        track.shareViaBluetooth();

        System.out.println("5) Лямбды:");
        PredicateValidator<String> notBlank = s -> s != null && !s.isBlank();
        PredicateValidator<Integer> isAdult = age -> age >= 18;
        for (String s : List.of("Евгений", "   ")) {
            System.out.println("   notBlank('" + s + "') = " + notBlank.validate(s));
        }
        System.out.println("   isAdult(17) = " + isAdult.validate(17) + ", isAdult(20) = " + isAdult.validate(20));
    }
}
