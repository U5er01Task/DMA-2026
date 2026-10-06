package pr2.topics;

/**
 * Тема 1. Базовый синтаксис, примитивные типы и операторы.
 */
public class Topic1Basics {

    // 1. Конвертер плотности (dp -> px)
    public static int dpToPx(float dp, float density) {
        return Math.round(dp * density);
    }

    // 2. Парсинг таймстемпа: из миллисекунд получаем часы, минуты, секунды
    public static String parseTimestamp(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return hours + " ч " + minutes + " мин " + seconds + " сек";
    }

    // 3. Расчет времени работы от батареи в часах
    public static double batteryLifeHours(int capacityMah, int radioMa, int displayMa) {
        int totalConsumption = radioMa + displayMa;
        if (totalConsumption <= 0) {
            return 0;
        }
        return (double) capacityMah / totalConsumption;
    }

    // 4. Валидатор координат
    public static boolean isValidCoordinates(double lat, double lon) {
        boolean latOk = lat >= -90.0 && lat <= 90.0;
        boolean lonOutOfRange = lon < -180.0 || lon > 180.0;
        return latOk && !lonOutOfRange;
    }

    // 5. Побитовые флаги разрешений
    public static final int CAMERA = 1;   // 001
    public static final int LOCATION = 2; // 010
    public static final int STORAGE = 4;  // 100

    public static int grant(int permissions, int flag) {
        return permissions | flag;
    }

    public static int revoke(int permissions, int flag) {
        return permissions & ~flag;
    }

    public static boolean has(int permissions, int flag) {
        return (permissions & flag) != 0;
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 1 ===");

        System.out.println("1) 48dp при hdpi (1.5) = " + dpToPx(48, 1.5f) + "px");
        System.out.println("   48dp при xhdpi (2.0) = " + dpToPx(48, 2.0f) + "px");
        System.out.println("   48dp при xxhdpi (3.0) = " + dpToPx(48, 3.0f) + "px");

        long millis = 9_876_543L;
        System.out.println("2) " + millis + " мс = " + parseTimestamp(millis));

        System.out.printf("3) Батарея 5000 мАч, связь 150 мА, экран 350 мА -> %.1f ч%n",
                batteryLifeHours(5000, 150, 350));

        System.out.println("4) (54.01, 38.29) валидны? " + isValidCoordinates(54.01, 38.29));
        System.out.println("   (95.0, 10.0) валидны? " + isValidCoordinates(95.0, 10.0));
        System.out.println("   (10.0, -200.0) валидны? " + isValidCoordinates(10.0, -200.0));

        int perms = 0;
        perms = grant(perms, CAMERA);
        perms = grant(perms, STORAGE);
        System.out.println("5) После выдачи CAMERA и STORAGE: " + Integer.toBinaryString(perms)
                + " (камера: " + has(perms, CAMERA) + ", гео: " + has(perms, LOCATION) + ")");
        perms = revoke(perms, CAMERA);
        System.out.println("   После отзыва CAMERA: " + Integer.toBinaryString(perms)
                + " (камера: " + has(perms, CAMERA) + ", хранилище: " + has(perms, STORAGE) + ")");
    }
}
