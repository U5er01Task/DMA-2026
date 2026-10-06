package pr2.topics;

import java.util.Arrays;
import java.util.Random;

/**
 * Тема 3. Массивы, строки и форматирование данных.
 */
public class Topic3Strings {

    // 1. Нормализация поискового запроса
    public static String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    // 2. Разворот массива на месте
    public static void reverseInPlace(String[] frames) {
        int left = 0;
        int right = frames.length - 1;
        while (left < right) {
            String tmp = frames[left];
            frames[left] = frames[right];
            frames[right] = tmp;
            left++;
            right--;
        }
    }

    // 3. Маскирование номера карты
    public static String maskCard(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException("Номер карты должен состоять из 16 цифр");
        }
        return "**** **** **** " + cardNumber.substring(12);
    }

    // 4. Максимальный скачок между соседними значениями
    public static double maxSpike(double[] values) {
        double max = 0;
        for (int i = 1; i < values.length; i++) {
            double diff = Math.abs(values[i] - values[i - 1]);
            if (diff > max) {
                max = diff;
            }
        }
        return max;
    }

    // 5. Сборка GET-параметров
    public static String buildQuery(String[] keys, String[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Длины массивов не совпадают");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            sb.append(i == 0 ? '?' : '&');
            sb.append(keys[i]).append('=').append(values[i]);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 3 ===");

        System.out.println("1) '" + normalizeQuery("   Чехол   ДЛЯ  iPhone    15  ") + "'");

        String[] frames = {"frame_01", "frame_02", "frame_03", "frame_04", "frame_05"};
        reverseInPlace(frames);
        System.out.println("2) " + Arrays.toString(frames));

        System.out.println("3) " + maskCard("4276380012341234"));

        // 100 измерений датчика (генерируем с фиксированным seed, чтобы результат повторялся)
        Random random = new Random(42);
        double[] sensor = new double[100];
        for (int i = 0; i < sensor.length; i++) {
            sensor[i] = 9.8 + random.nextGaussian();
        }
        sensor[57] = 19.5; // имитируем резкий удар
        System.out.printf("4) Максимальный всплеск: %.3f м/с^2%n", maxSpike(sensor));

        System.out.println("5) " + buildQuery(new String[]{"q", "page", "sort"},
                new String[]{"phone", "2", "price_asc"}));
    }
}
