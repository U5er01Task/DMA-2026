package pr2.practice;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Блок 3: Сеть, парсинг данных и офлайн-синхронизация (задания 21-30).
 */
public class Block3Network {

    // 21. Парсер диплинка
    public record DeepLink(String scheme, String host, String path, Map<String, String> params) {}

    public static DeepLink parseDeepLink(String url) {
        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            throw new IllegalArgumentException("Нет схемы в ссылке: " + url);
        }
        String scheme = url.substring(0, schemeEnd);
        String rest = url.substring(schemeEnd + 3);

        String query = "";
        int q = rest.indexOf('?');
        if (q >= 0) {
            query = rest.substring(q + 1);
            rest = rest.substring(0, q);
        }
        int slash = rest.indexOf('/');
        String host = slash >= 0 ? rest.substring(0, slash) : rest;
        String path = slash >= 0 ? rest.substring(slash) : "/";

        Map<String, String> params = new LinkedHashMap<>();
        if (!query.isEmpty()) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                params.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }
        return new DeepLink(scheme, host, path, params);
    }

    // 22. Retry-политика
    public interface NetworkCall {
        String execute() throws SocketTimeoutException;
    }

    public static String executeWithRetry(NetworkCall call, int maxRetries) throws SocketTimeoutException {
        SocketTimeoutException last = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    System.out.println("   повтор #" + attempt);
                }
                return call.execute();
            } catch (SocketTimeoutException e) {
                System.out.println("   таймаут: " + e.getMessage());
                last = e;
            }
        }
        throw last;
    }

    // 23. Очередь офлайн-действий
    public static class OfflineActionQueue {
        private final List<String> pending = new ArrayList<>();
        private boolean online;

        public void submit(String action) {
            if (online) {
                System.out.println("   сразу отправлено: " + action);
            } else {
                pending.add(action);
                System.out.println("   нет сети, сохранено: " + action);
            }
        }

        public void setOnline(boolean online) {
            this.online = online;
            if (online && !pending.isEmpty()) {
                System.out.println("   сеть появилась, отправляем пачку из " + pending.size() + ": " + pending);
                pending.clear();
            }
        }

        public int pendingCount() { return pending.size(); }
    }

    // 24. Разрешение конфликта версий заметки
    public record Note(String id, String text, int version, long updatedAt) {}

    public static String resolveConflict(Note local, Note server) {
        if (server.version() > local.version()) {
            return "UPDATE_LOCAL: берем серверную версию v" + server.version();
        }
        if (server.version() == local.version() && server.text().equals(local.text())) {
            return "NO_CHANGES";
        }
        return "PUSH_TO_SERVER: отправляем локальную версию v" + local.version() + " на перезапись";
    }

    // 25. Скорость скачивания
    public static String downloadSpeed(long bytes, long millis) {
        double seconds = millis / 1000.0;
        double kbPerSec = bytes / 1024.0 / seconds;
        double mbitPerSec = bytes * 8 / 1_000_000.0 / seconds;
        return String.format(Locale.US, "%.1f КБ/с, %.2f Мбит/с", kbPerSec, mbitPerSec);
    }

    // 26. Номер следующей страницы из заголовка Link
    private static final Pattern NEXT_LINK = Pattern.compile("<[^>]*[?&]page=(\\d+)[^>]*>;\\s*rel=\"next\"");

    public static Integer parseNextPage(String linkHeader) {
        if (linkHeader == null) return null;
        Matcher m = NEXT_LINK.matcher(linkHeader);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    // 27. Проверка ETag
    public record HttpResponse(int code, String body, String etag) {}

    public static class ETagServer {
        private String content = "{\"items\":[1,2,3]}";

        public void updateContent(String newContent) { content = newContent; }

        private String currentEtag() {
            return "\"" + Integer.toHexString(content.hashCode()) + "\"";
        }

        public HttpResponse get(String ifNoneMatch) {
            String etag = currentEtag();
            if (etag.equals(ifNoneMatch)) {
                return new HttpResponse(304, null, etag); // тело не передаем
            }
            return new HttpResponse(200, content, etag);
        }
    }

    // 28. Байты в читаемый вид
    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        String[] units = {"KB", "MB", "GB", "TB"};
        double value = bytes;
        int unit = -1;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return String.format(Locale.US, "%.1f %s", value, units[unit]);
    }

    // 29. Имитация веб-сокета котировок
    public interface QuoteListener {
        void onQuote(String pair, double price);
    }

    public static class QuoteGenerator {
        private final List<QuoteListener> listeners = new ArrayList<>();
        private final Random random = new Random(7);

        public void subscribe(QuoteListener l) { listeners.add(l); }

        public void start(String pair, double startPrice, int ticks, long intervalMs) throws InterruptedException {
            double price = startPrice;
            for (int i = 0; i < ticks; i++) {
                price += (random.nextDouble() - 0.5) * 0.8;
                for (QuoteListener l : listeners) {
                    l.onQuote(pair, price);
                }
                Thread.sleep(intervalMs);
            }
        }
    }

    // 30. Проверка обязательных полей профиля
    public static List<String> missingFields(Map<String, Object> json, String... required) {
        List<String> missing = new ArrayList<>();
        for (String field : required) {
            if (json.get(field) == null) {
                missing.add(field);
            }
        }
        return missing;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Блок 3 ===");

        System.out.println("21) " + parseDeepLink("app://shop/product?id=452&source=push"));

        System.out.println("22) Retry:");
        int[] counter = {0};
        String result = executeWithRetry(() -> {
            counter[0]++;
            if (counter[0] < 3) {
                throw new SocketTimeoutException("нет ответа за 10 с (вызов " + counter[0] + ")");
            }
            return "200 OK";
        }, 3);
        System.out.println("   результат: " + result);
        try {
            executeWithRetry(() -> { throw new SocketTimeoutException("сервер недоступен"); }, 3);
        } catch (SocketTimeoutException e) {
            System.out.println("   все попытки исчерпаны: " + e.getMessage());
        }

        System.out.println("23) Офлайн-очередь:");
        OfflineActionQueue offline = new OfflineActionQueue();
        offline.submit("LIKE post#10");
        offline.submit("COMMENT post#10 'Круто!'");
        offline.setOnline(true);
        offline.submit("LIKE post#11");

        Note local = new Note("n1", "Список покупок: хлеб", 3, 1000);
        System.out.println("24) " + resolveConflict(local, new Note("n1", "Список покупок: хлеб, молоко", 4, 2000)));
        System.out.println("    " + resolveConflict(local, new Note("n1", "Список покупок", 2, 500)));

        System.out.println("25) 15 728 640 байт за 4200 мс: " + downloadSpeed(15_728_640, 4200));

        System.out.println("26) next page = "
                + parseNextPage("<https://api.com/items?page=1>; rel=\"prev\", <https://api.com/items?page=3>; rel=\"next\""));

        ETagServer server = new ETagServer();
        HttpResponse first = server.get(null);
        HttpResponse second = server.get(first.etag());
        server.updateContent("{\"items\":[1,2,3,4]}");
        HttpResponse third = server.get(first.etag());
        System.out.println("27) первый: " + first.code() + ", повторный с ETag: " + second.code()
                + " (тело " + second.body() + "), после изменения: " + third.code());

        for (long b : new long[]{512, 1024, 1536, 1_048_576, 3_221_225_472L}) {
            System.out.println("28) " + b + " -> " + formatBytes(b));
        }

        System.out.println("29) Котировки:");
        QuoteGenerator ws = new QuoteGenerator();
        ws.subscribe((pair, price) -> System.out.printf(Locale.US, "   %s = %.2f%n", pair, price));
        ws.start("USD/RUB", 92.50, 4, 200);

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", 42);
        profile.put("name", "Evge");
        profile.put("email", null);
        System.out.println("30) не хватает полей: " + missingFields(profile, "id", "name", "email", "phone"));
    }
}
