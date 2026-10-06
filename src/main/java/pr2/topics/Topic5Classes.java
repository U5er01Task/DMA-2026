package pr2.topics;

/**
 * Тема 5. Классы, объекты и инкапсуляция.
 */
public class Topic5Classes {

    // 1. Модель экрана настроек
    public static class SettingsModel {
        private boolean isDarkMode;
        private int volumeLevel;
        private String appLanguage;

        public SettingsModel(boolean isDarkMode, int volumeLevel, String appLanguage) {
            this.isDarkMode = isDarkMode;
            setVolumeLevel(volumeLevel);
            this.appLanguage = appLanguage;
        }

        public boolean isDarkMode() { return isDarkMode; }

        public void setDarkMode(boolean darkMode) { this.isDarkMode = darkMode; }

        public int getVolumeLevel() { return volumeLevel; }

        public void setVolumeLevel(int volumeLevel) {
            if (volumeLevel < 0 || volumeLevel > 100) {
                throw new IllegalArgumentException("Громкость должна быть от 0 до 100, получено: " + volumeLevel);
            }
            this.volumeLevel = volumeLevel;
        }

        public String getAppLanguage() { return appLanguage; }

        public void setAppLanguage(String appLanguage) { this.appLanguage = appLanguage; }

        @Override
        public String toString() {
            return "Settings{dark=" + isDarkMode + ", volume=" + volumeLevel + ", lang=" + appLanguage + "}";
        }
    }

    // 2. DTO корзины
    public record CartItem(String id, String title, double price, int count) {
        public double total() {
            return price * count;
        }
    }

    // 3. Счетчик непрочитанных уведомлений
    public static class BadgeCounter {
        private int count;

        public int getCount() { return count; }

        public void increment() { count++; }

        public void decrement() {
            if (count > 0) {
                count--;
            }
        }

        public void setCount(int count) {
            this.count = Math.max(0, count);
        }
    }

    // 4. Таймер сессии
    public static class SessionTracker {
        private static final long TIMEOUT_MS = 15 * 60 * 1000;
        private final long loginTime;
        private long lastActionTime;

        public SessionTracker(long loginTime) {
            this.loginTime = loginTime;
            this.lastActionTime = loginTime;
        }

        public void registerAction(long time) {
            lastActionTime = time;
        }

        public boolean isExpired(long now) {
            return now - lastActionTime > TIMEOUT_MS;
        }

        public long getLoginTime() { return loginTime; }
    }

    // 5. Неизменяемая геопозиция
    public static final class GeoPoint {
        private final double latitude;
        private final double longitude;

        public GeoPoint(double latitude, double longitude) {
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Некорректная широта: " + latitude);
            }
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Некорректная долгота: " + longitude);
            }
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }

        public double getLongitude() { return longitude; }

        @Override
        public String toString() {
            return "GeoPoint(" + latitude + ", " + longitude + ")";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 5 ===");

        SettingsModel settings = new SettingsModel(true, 70, "ru");
        System.out.println("1) " + settings);
        try {
            settings.setVolumeLevel(150);
        } catch (IllegalArgumentException e) {
            System.out.println("   Ошибка: " + e.getMessage());
        }

        CartItem item = new CartItem("sku-17", "Наушники", 2990.0, 2);
        System.out.println("2) " + item + " -> итого " + item.total());

        BadgeCounter badge = new BadgeCounter();
        badge.increment();
        badge.increment();
        badge.decrement();
        badge.decrement();
        badge.decrement(); // в минус не уходит
        badge.setCount(-5);
        System.out.println("3) Счетчик после попыток уйти в минус: " + badge.getCount());

        long start = 1_700_000_000_000L;
        SessionTracker session = new SessionTracker(start);
        session.registerAction(start + 10 * 60 * 1000);
        System.out.println("4) Через 20 мин после входа истекла? " + session.isExpired(start + 20 * 60 * 1000));
        System.out.println("   Через 26 мин после входа истекла? " + session.isExpired(start + 26 * 60 * 1000));

        System.out.println("5) " + new GeoPoint(55.7558, 37.6173));
        try {
            new GeoPoint(123, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("   Ошибка: " + e.getMessage());
        }
    }
}
