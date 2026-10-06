package pr2.topics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Тема 9. Исключения и безопасность выполнения.
 */
public class Topic9Exceptions {

    // 1. Проверяемое исключение отсутствия сети
    public static class NoInternetException extends Exception {
        public NoInternetException(String message) {
            super(message);
        }
    }

    public static String fetchData(String url, boolean hasConnection) throws NoInternetException {
        if (!hasConnection) {
            throw new NoInternetException("Нет подключения к интернету при запросе " + url);
        }
        return "200 OK от " + url;
    }

    // 2. try-with-resources: читаем локальный конфиг
    public static Map<String, String> readConfig(String path) throws IOException {
        Map<String, String> config = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    config.put(parts[0].trim(), parts[1].trim());
                }
            }
        } // reader закроется автоматически
        return config;
    }

    // 3. Непроверяемое исключение для неверного возраста
    public static class InvalidUserDataException extends RuntimeException {
        public InvalidUserDataException(String message) {
            super(message);
        }
    }

    public static int parseAge(String ageStr) {
        int age;
        try {
            age = Integer.parseInt(ageStr.trim());
        } catch (NumberFormatException e) {
            throw new InvalidUserDataException("Возраст не является числом: '" + ageStr + "'");
        }
        if (age < 0 || age > 130) {
            throw new InvalidUserDataException("Недопустимый возраст: " + age);
        }
        return age;
    }

    // 4. Несколько блоков catch
    public static void riskyAccess(List<String> list, int index) {
        try {
            String value = list.get(index);
            System.out.println("   значение: " + value.toUpperCase());
        } catch (NullPointerException e) {
            System.out.println("   NullPointerException: список или элемент равен null");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("   IndexOutOfBoundsException: индекс " + index + " вне списка");
        } catch (Exception e) {
            System.out.println("   Другая ошибка: " + e);
        }
    }

    // 5. Безопасное чтение из "Bundle" (в консольной версии Bundle заменен на Map)
    public static String getStringSafe(Map<String, Object> bundle, String key, String defaultValue) {
        try {
            Object value = bundle.get(key);
            return value != null ? (String) value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 9 ===");

        System.out.println("1) Запросы:");
        for (boolean online : new boolean[]{true, false}) {
            try {
                System.out.println("   " + fetchData("https://api.example.com/feed", online));
            } catch (NoInternetException e) {
                System.out.println("   Поймано: " + e.getMessage());
            }
        }

        System.out.println("2) Чтение config.properties:");
        try {
            Map<String, String> config = readConfig("config.properties");
            System.out.println("   server.host = " + config.get("server.host"));
            System.out.println("   server.port = " + config.get("server.port"));
        } catch (IOException e) {
            System.out.println("   Не удалось прочитать конфиг: " + e.getMessage());
        }
        try {
            readConfig("missing.properties");
        } catch (IOException e) {
            System.out.println("   Отсутствующий файл: " + e.getMessage());
        }

        System.out.println("3) parseAge:");
        for (String s : new String[]{"25", "-3", "200", "abc"}) {
            try {
                System.out.println("   '" + s + "' -> " + parseAge(s));
            } catch (InvalidUserDataException e) {
                System.out.println("   '" + s + "' -> " + e.getMessage());
            }
        }

        System.out.println("4) Разные catch:");
        riskyAccess(List.of("wifi", "lte"), 1);
        riskyAccess(List.of("wifi", "lte"), 5);
        riskyAccess(null, 0);
        riskyAccess(java.util.Arrays.asList("a", null), 1);

        System.out.println("5) Безопасный Bundle:");
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("user_name", "Евгений");
        bundle.put("user_id", 42); // не строка
        System.out.println("   user_name = " + getStringSafe(bundle, "user_name", "Гость"));
        System.out.println("   user_id   = " + getStringSafe(bundle, "user_id", "нет"));
        System.out.println("   city      = " + getStringSafe(bundle, "city", "не указан"));
    }
}
