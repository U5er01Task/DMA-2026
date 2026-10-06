package pr2.topics;

import java.util.List;
import java.util.Locale;

/**
 * Тема 4. Методы и модульность.
 */
public class Topic4Methods {

    // 1. Перегрузка валидатора email
    public static boolean isValid(String email) {
        return email != null && email.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*");
    }

    public static boolean isValid(String email, boolean checkDomain) {
        if (!isValid(email)) {
            return false;
        }
        if (!checkDomain) {
            return true;
        }
        // при проверке домена требуем точку и зону минимум из 2 букв
        String domain = email.substring(email.indexOf('@') + 1);
        return domain.matches("([\\w-]+\\.)+[a-zA-Z]{2,}");
    }

    // 2. Форматирование валюты
    public static String formatPrice(double amount, String currencySymbol) {
        // русская локаль: пробел между разрядами и запятая перед копейками
        return String.format(Locale.of("ru", "RU"), "%,.2f %s", amount, currencySymbol);
    }

    // 3. Суммарный размер кэша в мегабайтах
    public static double calculateCache(long... fileSizesInBytes) {
        long total = 0;
        for (long size : fileSizesInBytes) {
            total += size;
        }
        return total / (1024.0 * 1024.0);
    }

    // 4. Рекурсивный подсчет элементов во вложенных папках
    public record Folder(String name, int files, List<Folder> subfolders) {}

    public static int countItems(Folder folder) {
        int count = folder.files();
        for (Folder sub : folder.subfolders()) {
            count += 1 + countItems(sub); // сама папка + ее содержимое
        }
        return count;
    }

    // 5. Сравнение версий приложения
    public static int compareVersions(String v1, String v2) {
        String[] a = v1.split("\\.");
        String[] b = v2.split("\\.");
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int x = i < a.length ? Integer.parseInt(a[i]) : 0;
            int y = i < b.length ? Integer.parseInt(b[i]) : 0;
            if (x != y) {
                return x > y ? 1 : -1;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 4 ===");

        System.out.println("1) isValid(\"evge@mail\") = " + isValid("evge@mail"));
        System.out.println("   isValid(\"evge@mail\", true) = " + isValid("evge@mail", true));
        System.out.println("   isValid(\"evge@mail.ru\", true) = " + isValid("evge@mail.ru", true));

        System.out.println("2) " + formatPrice(12499.9, "руб."));

        System.out.printf("3) Кэш: %.2f МБ%n", calculateCache(1_048_576, 524_288, 3_145_728, 20_480));

        Folder root = new Folder("DCIM", 2, List.of(
                new Folder("Camera", 5, List.of()),
                new Folder("Screenshots", 3, List.of(
                        new Folder("2025", 4, List.of())
                ))
        ));
        System.out.println("4) Всего элементов в DCIM: " + countItems(root));

        System.out.println("5) compare(1.12.0, 1.9.4) = " + compareVersions("1.12.0", "1.9.4"));
        System.out.println("   compare(2.0, 2.0.0) = " + compareVersions("2.0", "2.0.0"));
        System.out.println("   compare(3.1.2, 3.2) = " + compareVersions("3.1.2", "3.2"));
    }
}
