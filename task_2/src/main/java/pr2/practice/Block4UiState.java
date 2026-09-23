package pr2.practice;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

/**
 * Блок 4: Управление состоянием экрана и UI-архитектура (задания 31-40).
 */
public class Block4UiState {

    // 31. Состояния экрана через sealed-иерархию
    public sealed interface UiState permits Loading, Success, Empty, ErrorState {}

    public record Loading() implements UiState {}

    public record Success(List<String> data) implements UiState {}

    public record Empty() implements UiState {}

    public record ErrorState(String message) implements UiState {}

    public static String render(UiState state) {
        return switch (state) {
            case Loading l -> "показываем ProgressBar";
            case Success s -> "показываем список из " + s.data().size() + " элементов";
            case Empty e -> "показываем заглушку 'Здесь пока пусто'";
            case ErrorState err -> "показываем ошибку: " + err.message();
        };
    }

    public static UiState load(List<String> fromServer, Exception error) {
        if (error != null) return new ErrorState(error.getMessage());
        if (fromServer.isEmpty()) return new Empty();
        return new Success(fromServer);
    }

    // 32. Защита от двойного клика
    public static class DebouncedClick {
        private static final long INTERVAL_MS = 500;
        private final Runnable action;
        private long lastClick = -INTERVAL_MS;

        public DebouncedClick(Runnable action) { this.action = action; }

        public boolean click(long now) {
            if (now - lastClick < INTERVAL_MS) {
                return false;
            }
            lastClick = now;
            action.run();
            return true;
        }
    }

    // 33. Свой стек навигации
    public static class BackStack {
        private final List<String> screens = new ArrayList<>();

        public void push(String screen) { screens.add(screen); }

        public String pop() {
            if (screens.size() <= 1) {
                return null; // корень не удаляем, иначе приложение закроется
            }
            return screens.remove(screens.size() - 1);
        }

        public void popToRoot() {
            while (screens.size() > 1) {
                screens.remove(screens.size() - 1);
            }
        }

        public String current() { return screens.isEmpty() ? null : screens.get(screens.size() - 1); }

        @Override
        public String toString() { return screens.toString(); }
    }

    // 34. Палитра светлой и темной темы
    public enum ThemeMode { LIGHT, DARK }

    public static class ThemePalette {
        private final ThemeMode mode;

        public ThemePalette(ThemeMode mode) { this.mode = mode; }

        public String background() { return mode == ThemeMode.DARK ? "#121212" : "#FFFFFF"; }

        public String surface() { return mode == ThemeMode.DARK ? "#1E1E1E" : "#F5F5F5"; }

        public String textPrimary() { return mode == ThemeMode.DARK ? "#E6E6E6" : "#1A1A1A"; }

        public String accent() { return mode == ThemeMode.DARK ? "#8AB4F8" : "#1A73E8"; }

        @Override
        public String toString() {
            return mode + "{bg=" + background() + ", surface=" + surface()
                    + ", text=" + textPrimary() + ", accent=" + accent() + "}";
        }
    }

    // 35. Процент заполнения профиля
    public record Profile(String avatarUrl, String bio, String phone, String email, String city) {}

    public static int profileCompletion(Profile p) {
        String[] fields = {p.avatarUrl(), p.bio(), p.phone(), p.email(), p.city()};
        int filled = 0;
        for (String f : fields) {
            if (f != null && !f.isBlank()) filled++;
        }
        return filled * 100 / fields.length;
    }

    // 36. Цвет текста по яркости фона (YIQ)
    public static String textColorFor(int r, int g, int b) {
        int yiq = (r * 299 + g * 587 + b * 114) / 1000;
        return yiq >= 128 ? "#000000" : "#FFFFFF";
    }

    // 37. Счетчик лайков
    public static String formatCount(long n) {
        if (n < 1000) return String.valueOf(n);
        if (n < 1_000_000) return trimZero(n / 1000.0) + "K";
        return trimZero(n / 1_000_000.0) + "M";
    }

    private static String trimZero(double value) {
        double rounded = Math.floor(value * 10) / 10; // 1250 -> 1.2K, а не 1.3K
        return rounded == (long) rounded
                ? String.valueOf((long) rounded)
                : String.format(Locale.US, "%.1f", rounded);
    }

    // 38. Очередь диалогов
    public static class DialogManager {
        private final Queue<String> queue = new ArrayDeque<>();
        private String showing;

        public void show(String dialog) {
            if (showing == null) {
                showing = dialog;
                System.out.println("   показан диалог: " + dialog);
            } else {
                queue.add(dialog);
                System.out.println("   в очередь: " + dialog);
            }
        }

        public void dismiss() {
            System.out.println("   закрыт: " + showing);
            showing = queue.poll();
            if (showing != null) {
                System.out.println("   показан диалог: " + showing);
            }
        }
    }

    // 39. Активность кнопки "Оплатить"
    public static boolean isPayEnabled(int cartSize, String paymentMethod, boolean addressConfirmed) {
        return cartSize > 0 && paymentMethod != null && addressConfirmed;
    }

    // 40. Ошибки для пользователя
    public static String userMessage(Throwable e) {
        if (e instanceof TimeoutException || e instanceof SocketTimeoutException) {
            return "Сервер долго не отвечает. Проверьте интернет и попробуйте еще раз.";
        }
        if (e instanceof UnknownHostException) {
            return "Нет подключения к интернету. Включите Wi-Fi или мобильные данные.";
        }
        if (e instanceof SecurityException) {
            return "Приложению не хватает разрешений. Выдайте их в настройках.";
        }
        return "Что-то пошло не так. Мы уже разбираемся.";
    }

    public static void main(String[] args) {
        System.out.println("=== Блок 4 ===");

        List<UiState> states = List.of(
                new Loading(),
                load(List.of("a", "b", "c"), null),
                load(List.of(), null),
                load(null, new RuntimeException("HTTP 500")));
        for (UiState s : states) {
            System.out.println("31) " + s + " -> " + render(s));
        }

        int[] payments = {0};
        DebouncedClick pay = new DebouncedClick(() -> payments[0]++);
        long[] clicks = {0, 120, 300, 650, 700, 1300};
        for (long t : clicks) {
            pay.click(t);
        }
        System.out.println("32) кликов " + clicks.length + ", выполнено действий: " + payments[0]);

        BackStack stack = new BackStack();
        stack.push("Home");
        stack.push("Catalog");
        stack.push("Product");
        stack.push("Cart");
        System.out.println("33) " + stack + ", pop -> " + stack.pop() + ", текущий " + stack.current());
        stack.popToRoot();
        System.out.println("    после popToRoot: " + stack + ", pop на корне -> " + stack.pop());

        System.out.println("34) " + new ThemePalette(ThemeMode.LIGHT));
        System.out.println("    " + new ThemePalette(ThemeMode.DARK));

        System.out.println("35) заполнено: "
                + profileCompletion(new Profile("avatar.png", "", "+79990001122", "evge@mail.ru", null)) + "%");

        System.out.println("36) фон #FFD600 -> текст " + textColorFor(255, 214, 0)
                + ", фон #283593 -> текст " + textColorFor(40, 53, 147));

        for (long n : new long[]{950, 1200, 1000, 15_300, 1_500_000}) {
            System.out.println("37) " + n + " -> " + formatCount(n));
        }

        System.out.println("38) Диалоги:");
        DialogManager dialogs = new DialogManager();
        dialogs.show("Разрешение на геолокацию");
        dialogs.show("Оцените приложение");
        dialogs.dismiss();
        dialogs.dismiss();

        System.out.println("39) пустая корзина: " + isPayEnabled(0, "card", true)
                + ", нет оплаты: " + isPayEnabled(2, null, true)
                + ", все ок: " + isPayEnabled(2, "sbp", true));

        List<Throwable> errors = List.of(new TimeoutException(), new UnknownHostException("api.shop.ru"),
                new IllegalStateException());
        for (Throwable e : errors) {
            System.out.println("40) " + e.getClass().getSimpleName() + " -> " + userMessage(e));
        }
    }
}
