package pr2.topics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Тема 8. Коллекции и Generics.
 */
public class Topic8Collections {

    // 1. Удаление дубликатов с сохранением порядка
    public static Set<String> uniqueContacts(List<String> phones) {
        return new LinkedHashSet<>(phones);
    }

    // 2. Очередь синхронизации FIFO
    public static void processSyncQueue(Queue<String> queue) {
        while (!queue.isEmpty()) {
            String request = queue.poll();
            System.out.println("   отправлен: " + request + " (осталось " + queue.size() + ")");
        }
    }

    // 3. Обобщенный ответ API
    public static class ApiResponse<T> {
        private final int statusCode;
        private final T data;
        private final String errorMessage;

        public ApiResponse(int statusCode, T data, String errorMessage) {
            this.statusCode = statusCode;
            this.data = data;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }

        public int getStatusCode() { return statusCode; }

        public T getData() { return data; }

        public String getErrorMessage() { return errorMessage; }
    }

    // 4. Сортировка товаров
    public record Product(String title, double price, double rating) {}

    public static void sortProducts(List<Product> products) {
        // по возрастанию цены, при равной цене сначала с большим рейтингом
        products.sort(Comparator.comparingDouble(Product::price)
                .thenComparing(Comparator.comparingDouble(Product::rating).reversed()));
    }

    // 5. LRU-кэш фрагментов на LinkedHashMap
    public static class ScreenCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public ScreenCache(int capacity) {
            super(16, 0.75f, true); // accessOrder = true -> порядок по последнему обращению
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 8 ===");

        List<String> phones = List.of("+79990001122", "+79161234567", "+79990001122", "+79035550000", "+79161234567");
        System.out.println("1) " + uniqueContacts(phones));

        Queue<String> queue = new ArrayDeque<>();
        queue.offer("POST /likes/15");
        queue.offer("POST /comments/7");
        queue.offer("PUT /profile");
        System.out.println("2) Очередь синхронизации:");
        processSyncQueue(queue);

        ApiResponse<String> ok = new ApiResponse<>(200, "{\"name\":\"Evge\"}", null);
        ApiResponse<String> fail = new ApiResponse<>(404, null, "Пользователь не найден");
        System.out.println("3) ok.isSuccessful() = " + ok.isSuccessful() + ", data = " + ok.getData());
        System.out.println("   fail.isSuccessful() = " + fail.isSuccessful() + ", error = " + fail.getErrorMessage());

        List<Product> products = new ArrayList<>(List.of(
                new Product("Чехол", 990, 4.1),
                new Product("Кабель USB-C", 490, 4.7),
                new Product("Стекло", 990, 4.8),
                new Product("Зарядка", 1990, 4.5),
                new Product("Держатель", 490, 3.9)
        ));
        sortProducts(products);
        System.out.println("4) Отсортировано:");
        for (Product p : products) {
            System.out.println("   " + p.title() + " - " + p.price() + " руб., рейтинг " + p.rating());
        }

        ScreenCache<String, String> cache = new ScreenCache<>(5);
        for (String screen : List.of("Home", "Catalog", "Product", "Cart", "Profile")) {
            cache.put(screen, "fragment_" + screen.toLowerCase());
        }
        cache.get("Home"); // Home стал самым свежим
        cache.put("Settings", "fragment_settings"); // вытесняется Catalog
        System.out.println("5) Кэш экранов: " + cache.keySet());
    }
}
