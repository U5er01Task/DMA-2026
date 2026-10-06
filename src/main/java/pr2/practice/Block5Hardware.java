package pr2.practice;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Блок 5: Аппаратные функции, геолокация и фоновые задачи (задания 41-50).
 */
public class Block5Hardware {

    private static final double EARTH_RADIUS_M = 6_371_000;

    // 41. Формула гаверсинусов
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    // 42. Геозона
    public static boolean isInsideGeofence(double lat, double lon, double centerLat, double centerLon, double radiusM) {
        return haversine(lat, lon, centerLat, centerLon) <= radiusM;
    }

    // 43. Интервал опроса GPS от заряда
    public static int gpsIntervalSeconds(int batteryPercent) {
        if (batteryPercent > 50) return 5;
        if (batteryPercent >= 15) return 30;
        return 300;
    }

    // 44. Детектор падения
    public static String detectFall(double x, double y, double z) {
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        if (magnitude < 2.0) return String.format(Locale.US, "НЕВЕСОМОСТЬ (%.1f м/с^2) - телефон падает", magnitude);
        if (magnitude > 25.0) return String.format(Locale.US, "УДАР (%.1f м/с^2)", magnitude);
        return String.format(Locale.US, "норма (%.1f м/с^2)", magnitude);
    }

    // 45. Шагомер по локальным максимумам
    public static int countSteps(double[] verticalAcc, double threshold) {
        int steps = 0;
        for (int i = 1; i < verticalAcc.length - 1; i++) {
            boolean isPeak = verticalAcc[i] > verticalAcc[i - 1] && verticalAcc[i] >= verticalAcc[i + 1];
            if (isPeak && verticalAcc[i] > threshold) {
                steps++;
            }
        }
        return steps;
    }

    // 46. Яркость по освещенности (логарифмическая шкала, 1 лк -> 0%, 10 000 лк -> 100%)
    public static int brightnessPercent(double lux) {
        if (lux <= 1) return 0;
        double percent = Math.log10(lux) / Math.log10(10_000) * 100;
        return (int) Math.round(Math.min(100, percent));
    }

    // 47. Можно ли запускать тяжелую синхронизацию
    public static boolean canStartHeavySync(boolean wifiConnected, boolean charging) {
        return wifiConnected && charging;
    }

    // 48. Монитор трафика
    public static class TrafficMonitor {
        private static final long LIMIT = 5L * 1024 * 1024 * 1024;
        private long mobileBytes;
        private long wifiBytes;
        private boolean warned;

        public void addMobile(long bytes) {
            mobileBytes += bytes;
            if (!warned && mobileBytes >= LIMIT) {
                warned = true;
                System.out.println("   !! достигнут лимит мобильного трафика 5 ГБ!");
            }
        }

        public void addWifi(long bytes) { wifiBytes += bytes; }

        public String report() {
            return String.format(Locale.US, "мобильный: %.2f ГБ, Wi-Fi: %.2f ГБ",
                    mobileBytes / 1073741824.0, wifiBytes / 1073741824.0);
        }
    }

    // 49. Конечный автомат аудиоплеера
    public enum PlayerState { IDLE, INITIALIZED, PREPARED, PLAYING, PAUSED, STOPPED }

    public static class AudioPlayer {
        private static final Map<PlayerState, Set<PlayerState>> ALLOWED = Map.of(
                PlayerState.IDLE, EnumSet.of(PlayerState.INITIALIZED),
                PlayerState.INITIALIZED, EnumSet.of(PlayerState.PREPARED, PlayerState.IDLE),
                PlayerState.PREPARED, EnumSet.of(PlayerState.PLAYING, PlayerState.STOPPED),
                PlayerState.PLAYING, EnumSet.of(PlayerState.PAUSED, PlayerState.STOPPED),
                PlayerState.PAUSED, EnumSet.of(PlayerState.PLAYING, PlayerState.STOPPED),
                PlayerState.STOPPED, EnumSet.of(PlayerState.PREPARED, PlayerState.IDLE)
        );

        private PlayerState state = PlayerState.IDLE;

        public boolean moveTo(PlayerState next) {
            if (!ALLOWED.get(state).contains(next)) {
                System.out.println("   x переход " + state + " -> " + next + " запрещен");
                return false;
            }
            System.out.println("   " + state + " -> " + next);
            state = next;
            return true;
        }

        public PlayerState getState() { return state; }
    }

    // 50. Отчет о падении приложения
    public static String crashReport(Throwable error, String androidVersion, String deviceModel, long freeBytes) {
        StringWriter trace = new StringWriter();
        error.printStackTrace(new PrintWriter(trace));
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "===== CRASH REPORT =====\n"
                + "Время:        " + time + "\n"
                + "Android:      " + androidVersion + "\n"
                + "Устройство:   " + deviceModel + "\n"
                + "Свободно:     " + String.format(Locale.US, "%.2f ГБ", freeBytes / 1073741824.0) + "\n"
                + "Исключение:   " + error + "\n"
                + "Стектрейс:\n" + trace
                + "========================";
    }

    public static void main(String[] args) {
        System.out.println("=== Блок 5 ===");

        double user = haversine(55.7558, 37.6173, 55.7520, 37.6175);
        System.out.printf(Locale.US, "41) пользователь - курьер: %.0f м%n", user);
        System.out.printf(Locale.US, "    Москва - Санкт-Петербург: %.0f км%n",
                haversine(55.7558, 37.6173, 59.9343, 30.3351) / 1000);

        System.out.println("42) точка в 420 м от центра, радиус 500 м: "
                + isInsideGeofence(55.7520, 37.6175, 55.7558, 37.6173, 500));
        System.out.println("    та же точка, радиус 300 м: "
                + isInsideGeofence(55.7520, 37.6175, 55.7558, 37.6173, 300));

        for (int battery : new int[]{87, 50, 15, 9}) {
            System.out.println("43) заряд " + battery + "% -> опрос каждые " + gpsIntervalSeconds(battery) + " с");
        }

        System.out.println("44) " + detectFall(0.1, 9.7, 0.4));
        System.out.println("    " + detectFall(0.3, 0.5, 0.2));
        System.out.println("    " + detectFall(18.0, 22.0, 5.0));

        double[] walk = {0.1, 1.8, 0.3, -0.9, 0.2, 2.1, 0.5, -1.1, 0.4, 0.9, 0.3, -0.8, 0.6, 1.9, 0.2};
        System.out.println("45) шагов: " + countSteps(walk, 1.5));

        for (double lux : new double[]{0.5, 10, 300, 10_000, 50_000}) {
            System.out.println("46) " + lux + " лк -> яркость " + brightnessPercent(lux) + "%");
        }

        System.out.println("47) Wi-Fi + зарядка: " + canStartHeavySync(true, true)
                + ", только Wi-Fi: " + canStartHeavySync(true, false));

        System.out.println("48) Трафик:");
        TrafficMonitor traffic = new TrafficMonitor();
        traffic.addWifi(12L * 1024 * 1024 * 1024);
        traffic.addMobile(3L * 1024 * 1024 * 1024);
        System.out.println("   " + traffic.report());
        traffic.addMobile(2200L * 1024 * 1024);
        System.out.println("   " + traffic.report());

        System.out.println("49) Плеер:");
        AudioPlayer player = new AudioPlayer();
        player.moveTo(PlayerState.PLAYING); // нельзя сразу играть
        player.moveTo(PlayerState.INITIALIZED);
        player.moveTo(PlayerState.PREPARED);
        player.moveTo(PlayerState.PLAYING);
        player.moveTo(PlayerState.PAUSED);
        player.moveTo(PlayerState.INITIALIZED); // запрещено
        player.moveTo(PlayerState.PLAYING);
        player.moveTo(PlayerState.STOPPED);
        System.out.println("   итоговое состояние: " + player.getState());

        System.out.println("50) Краш-репорт:");
        try {
            String[] items = new String[2];
            System.out.println(items[0].length());
        } catch (NullPointerException e) {
            System.out.println(crashReport(e, "Android 14 (API 34)", "Pixel 7",
                    new File(".").getUsableSpace()));
        }
    }
}
