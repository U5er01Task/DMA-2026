package pr2.topics;

import java.util.Scanner;

/**
 * Тема 2. Управляющие конструкции и операторы ветвления.
 */
public class Topic2ControlFlow {

    // 1. Ориентация экрана
    public static String orientation(int width, int height) {
        if (width > height) {
            return "LANDSCAPE";
        } else if (width < height) {
            return "PORTRAIT";
        }
        return "SQUARE";
    }

    // 2. Категория HTTP-ответа через switch
    public static String httpCategory(int code) {
        return switch (code / 100) {
            case 1 -> "Информационный";
            case 2 -> "Успешный";
            case 3 -> "Перенаправление";
            case 4 -> "Ошибка клиента";
            case 5 -> "Ошибка сервера";
            default -> "Неизвестный код";
        };
    }

    // 3. Экспоненциальная задержка между попытками
    public static void backoffSimulation() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            int delaySec = 1 << (attempt - 1); // 1, 2, 4, 8, 16
            System.out.println("   Попытка " + attempt + ": ждем " + delaySec + " с перед подключением");
        }
    }

    // 4. Пропуск поврежденных пакетов (-1 -> continue, 0 -> break)
    public static void processMessages(int[] ids) {
        for (int id : ids) {
            if (id == -1) {
                System.out.println("   пакет поврежден, пропускаем");
                continue;
            }
            if (id == 0) {
                System.out.println("   конец сессии");
                break;
            }
            System.out.println("   обработано сообщение #" + id);
        }
    }

    // 5. Проверка пин-кода через do-while, максимум 3 попытки
    public static boolean checkPin(String correctPin, Scanner input) {
        int attempts = 0;
        boolean success;
        do {
            attempts++;
            System.out.print("   Введите PIN (попытка " + attempts + "/3): ");
            String entered = input.hasNextLine() ? input.nextLine().trim() : "";
            System.out.println(entered);
            success = entered.matches("\\d{4}") && entered.equals(correctPin);
            if (!success) {
                System.out.println("   Неверный PIN");
            }
        } while (!success && attempts < 3);

        System.out.println(success ? "   Доступ разрешен" : "   Устройство заблокировано");
        return success;
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 2 ===");

        System.out.println("1) 1080x2400 -> " + orientation(1080, 2400));
        System.out.println("   2400x1080 -> " + orientation(2400, 1080));
        System.out.println("   800x800 -> " + orientation(800, 800));

        int[] codes = {101, 200, 301, 404, 503, 999};
        for (int code : codes) {
            System.out.println("2) " + code + " -> " + httpCategory(code));
        }

        System.out.println("3) Backoff:");
        backoffSimulation();

        System.out.println("4) Обработка сообщений чата:");
        processMessages(new int[]{15, 16, -1, 17, 0, 18});

        System.out.println("5) Проверка PIN (правильный 4821), ввод идет из заготовленных строк:");
        // чтобы демо не ждало ввода с клавиатуры, подаем строки как будто их набрал пользователь
        checkPin("4821", new Scanner("1111\n48a1\n4821\n"));
    }
}
