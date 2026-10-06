package pr2.practice;

import java.security.SecureRandom;

/**
 * Блок 1: Авторизация, безопасность и валидация ввода (задания 1-10).
 */
public class Block1Auth {

    // 1. Надежность пароля
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#$%^&*".indexOf(c) >= 0) hasSpecial = true;
        }
        return hasUpper && hasDigit && hasSpecial;
    }

    // 2. Нормализация номера в E.164 (российские номера)
    public static String toE164(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11 && (digits.startsWith("8") || digits.startsWith("7"))) {
            return "+7" + digits.substring(1);
        }
        if (digits.length() == 10 && digits.startsWith("9")) {
            return "+7" + digits;
        }
        throw new IllegalArgumentException("Не удалось распознать номер: " + phone);
    }

    // 3. 6-значный OTP-код
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    // 4. Проверка срока действия JWT
    public static boolean isTokenActive(long expSeconds) {
        return isTokenActive(expSeconds, System.currentTimeMillis() / 1000);
    }

    public static boolean isTokenActive(long expSeconds, long nowSeconds) {
        return nowSeconds < expSeconds;
    }

    // 5. Маскировка email
    public static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            throw new IllegalArgumentException("Некорректный email: " + email);
        }
        String local = email.substring(0, at);
        if (local.length() <= 2) {
            return local.charAt(0) + "*" + email.substring(at);
        }
        return local.charAt(0) + "*".repeat(local.length() - 2) + local.charAt(local.length() - 1) + email.substring(at);
    }

    // 6. Защита от перебора пароля
    public static class LoginThrottler {
        private static final int MAX_ATTEMPTS = 5;
        private static final long LOCK_MS = 60_000;
        private int failedAttempts;
        private long lockedUntil;

        public boolean isLocked(long now) {
            return now < lockedUntil;
        }

        public long secondsLeft(long now) {
            return Math.max(0, (lockedUntil - now + 999) / 1000);
        }

        /** @return true, если попытка вообще была принята к проверке */
        public boolean tryLogin(boolean passwordCorrect, long now) {
            if (isLocked(now)) {
                return false;
            }
            if (passwordCorrect) {
                failedAttempts = 0;
                return true;
            }
            failedAttempts++;
            if (failedAttempts >= MAX_ATTEMPTS) {
                lockedUntil = now + LOCK_MS;
                failedAttempts = 0;
            }
            return true;
        }
    }

    // 7. Обратимый шифр перестановкой: меняем местами символы в парах и разворачиваем строку
    public static String encrypt(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i + 1 < chars.length; i += 2) {
            char tmp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = tmp;
        }
        return new StringBuilder(new String(chars)).reverse().toString();
    }

    public static String decrypt(String cipher) {
        char[] chars = new StringBuilder(cipher).reverse().toString().toCharArray();
        for (int i = 0; i + 1 < chars.length; i += 2) {
            char tmp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = tmp;
        }
        return new String(chars);
    }

    // 8. Готовность биометрии
    public static String biometricStatus(boolean hasSensor, boolean hasPermission,
                                         boolean fingerprintEnrolled, boolean screenLockSet) {
        if (!hasSensor) return "Нет сканера отпечатка";
        if (!hasPermission) return "Нет разрешения USE_BIOMETRIC";
        if (!screenLockSet) return "Не установлена блокировка экрана";
        if (!fingerprintEnrolled) return "Отпечаток не добавлен в настройках";
        return "Биометрия доступна";
    }

    // 9. Сброс экрана при неактивности > 3 минут
    public static class InactivityWatcher {
        private static final long TIMEOUT_MS = 3 * 60 * 1000;
        private long lastInteraction;
        private String screenState = "Главный экран";

        public InactivityWatcher(long now) { this.lastInteraction = now; }

        public void onUserInteraction(long now) { lastInteraction = now; }

        public void tick(long now) {
            if (now - lastInteraction > TIMEOUT_MS) {
                screenState = "Экран блокировки (состояние сброшено)";
            }
        }

        public String getScreenState() { return screenState; }
    }

    // 10. Промокод вида ABCD-1234
    public static boolean isValidPromo(String code) {
        return code != null && code.matches("[A-Z]{4}-\\d{4}");
    }

    public static void main(String[] args) {
        System.out.println("=== Блок 1 ===");

        for (String p : new String[]{"qwerty", "Password1", "Passw0rd!", "evge2026#A"}) {
            System.out.println("1) '" + p + "' надежный? " + isStrongPassword(p));
        }

        for (String ph : new String[]{"8 (999) 000-11-22", "+7 999 000 11 22", "9990001122"}) {
            System.out.println("2) " + ph + " -> " + toE164(ph));
        }

        String otp = generateOtp();
        System.out.println("3) OTP: " + otp + " (длина " + otp.length() + ")");

        long now = 1_790_000_000L;
        System.out.println("4) exp=now+600 активен? " + isTokenActive(now + 600, now));
        System.out.println("   exp=now-1 активен? " + isTokenActive(now - 1, now));

        System.out.println("5) " + maskEmail("alexander.ivanov@mail.ru"));

        System.out.println("6) LoginThrottler:");
        LoginThrottler throttler = new LoginThrottler();
        long t = 0;
        for (int i = 1; i <= 5; i++) {
            throttler.tryLogin(false, t);
            t += 1000;
        }
        System.out.println("   после 5 ошибок заблокирован? " + throttler.isLocked(t)
                + ", осталось " + throttler.secondsLeft(t) + " с");
        System.out.println("   попытка во время блокировки принята? " + throttler.tryLogin(true, t + 10_000));
        System.out.println("   через 61 с заблокирован? " + throttler.isLocked(t + 61_000));

        String note = "Купить подарок маме";
        String enc = encrypt(note);
        System.out.println("7) '" + note + "' -> '" + enc + "' -> '" + decrypt(enc) + "'");

        System.out.println("8) " + biometricStatus(true, true, false, true));
        System.out.println("   " + biometricStatus(true, true, true, true));

        long start = 0;
        InactivityWatcher watcher = new InactivityWatcher(start);
        watcher.onUserInteraction(start + 60_000);
        watcher.tick(start + 150_000);
        System.out.println("9) через 1.5 мин после действия: " + watcher.getScreenState());
        watcher.tick(start + 60_000 + 181_000);
        System.out.println("   через 3 мин 1 с: " + watcher.getScreenState());

        for (String code : new String[]{"SALE-2026", "sale-2026", "SAL-20266", "PROMO-1234"}) {
            System.out.println("10) " + code + " -> " + isValidPromo(code));
        }
    }
}
