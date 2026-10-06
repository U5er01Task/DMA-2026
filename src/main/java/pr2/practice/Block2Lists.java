package pr2.practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Блок 2: Работа со списками, каталогами и кэшем (задания 11-20).
 */
public class Block2Lists {

    // 11. Сравнение старого и нового списка новостей
    public record NewsItem(long id, String title, String body) {}

    public record DiffResult(List<Long> changed, List<Long> removed, List<Long> added) {}

    public static DiffResult diff(List<NewsItem> oldList, List<NewsItem> newList) {
        Map<Long, NewsItem> newById = new HashMap<>();
        for (NewsItem item : newList) {
            newById.put(item.id(), item);
        }
        List<Long> changed = new ArrayList<>();
        List<Long> removed = new ArrayList<>();
        Map<Long, Boolean> seen = new HashMap<>();
        for (NewsItem old : oldList) {
            seen.put(old.id(), true);
            NewsItem fresh = newById.get(old.id());
            if (fresh == null) {
                removed.add(old.id());
            } else if (!fresh.equals(old)) { // record сравнивает все поля
                changed.add(old.id());
            }
        }
        List<Long> added = new ArrayList<>();
        for (NewsItem item : newList) {
            if (!seen.containsKey(item.id())) {
                added.add(item.id());
            }
        }
        return new DiffResult(changed, removed, added);
    }

    // 12. Пагинация
    public static class PaginationHelper<T> {
        private final List<T> all;
        private final int pageSize;

        public PaginationHelper(List<T> all) { this(all, 20); }

        public PaginationHelper(List<T> all, int pageSize) {
            this.all = all;
            this.pageSize = pageSize;
        }

        /** Страницы нумеруются с 1. */
        public List<T> getPage(int page) {
            int from = (page - 1) * pageSize;
            if (page < 1 || from >= all.size()) {
                return List.of();
            }
            int to = Math.min(from + pageSize, all.size());
            return all.subList(from, to);
        }

        public int pageCount() {
            return (all.size() + pageSize - 1) / pageSize;
        }
    }

    // 13. Группировка контактов по первой букве
    public static Map<Character, List<String>> groupByLetter(List<String> names) {
        Map<Character, List<String>> groups = new TreeMap<>();
        for (String name : names) {
            if (name == null || name.isBlank()) continue;
            char letter = Character.toUpperCase(name.trim().charAt(0));
            groups.computeIfAbsent(letter, k -> new ArrayList<>()).add(name.trim());
        }
        for (List<String> list : groups.values()) {
            list.sort(String::compareToIgnoreCase);
        }
        return groups;
    }

    // 14. Фильтр каталога
    public record CatalogItem(String sku, String name, double price) {}

    public static List<CatalogItem> filter(List<CatalogItem> items, String query) {
        String q = query.trim().toLowerCase();
        List<CatalogItem> result = new ArrayList<>();
        for (CatalogItem item : items) {
            if (item.name().toLowerCase().contains(q) || item.sku().toLowerCase().contains(q)) {
                result.add(item);
            }
        }
        return result;
    }

    // 15. Карусель баннеров
    public static int nextBanner(int currentIndex, int size) {
        return (currentIndex + 1) % size;
    }

    // 16. Сумма корзины со скидками по категориям
    public record CartPosition(String title, String category, double price, int qty) {}

    public static double cartTotal(List<CartPosition> cart, Map<String, Integer> discountPercentByCategory) {
        double total = 0;
        for (CartPosition p : cart) {
            int discount = discountPercentByCategory.getOrDefault(p.category(), 0);
            total += p.price() * p.qty() * (100 - discount) / 100.0;
        }
        return Math.round(total * 100) / 100.0;
    }

    // 17. Удаление свайпом с отменой
    public static class UndoableList<T> {
        private static final long UNDO_WINDOW_MS = 4000;
        private final List<T> items;
        private T pendingItem;
        private int pendingIndex = -1;
        private long deletedAt;

        public UndoableList(List<T> items) { this.items = new ArrayList<>(items); }

        public void swipeDelete(int index, long now) {
            if (pendingItem != null) { // новый свайп сразу фиксирует предыдущее удаление
                System.out.println("   удаление '" + pendingItem + "' зафиксировано в базе");
            }
            pendingItem = items.remove(index);
            pendingIndex = index;
            deletedAt = now;
        }

        public boolean undo(long now) {
            if (pendingItem == null || now - deletedAt > UNDO_WINDOW_MS) {
                return false;
            }
            items.add(pendingIndex, pendingItem);
            pendingItem = null;
            return true;
        }

        /** Вызывается по таймеру: после окна отмены удаление становится окончательным. */
        public void commit(long now) {
            if (pendingItem != null && now - deletedAt > UNDO_WINDOW_MS) {
                System.out.println("   удаление '" + pendingItem + "' зафиксировано в базе");
                pendingItem = null;
            }
        }

        public List<T> getItems() { return items; }
    }

    // 18. Сортировка чатов
    public record Dialog(String title, long lastMessageTime) {}

    public static void sortDialogs(List<Dialog> dialogs) {
        dialogs.sort(Comparator.comparingLong(Dialog::lastMessageTime).reversed());
    }

    // 19. Дубликаты в галерее по имени и размеру
    public record MediaFile(String path, String name, long size) {}

    public static List<List<MediaFile>> findDuplicates(List<MediaFile> files) {
        Map<String, List<MediaFile>> groups = new HashMap<>();
        for (MediaFile f : files) {
            String key = f.name().toLowerCase() + "|" + f.size();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(f);
        }
        List<List<MediaFile>> result = new ArrayList<>();
        for (List<MediaFile> group : groups.values()) {
            if (group.size() > 1) {
                result.add(group);
            }
        }
        return result;
    }

    // 20. FIFO-кэш картинок с лимитом 100 МБ
    public static class ImageCache {
        private static final long LIMIT = 100L * 1024 * 1024;
        private final Deque<String> order = new ArrayDeque<>();
        private final Map<String, Long> sizes = new HashMap<>();
        private long totalSize;

        public void put(String key, long size) {
            if (sizes.containsKey(key)) return;
            order.addLast(key);
            sizes.put(key, size);
            totalSize += size;
            while (totalSize > LIMIT && !order.isEmpty()) {
                String oldest = order.removeFirst();
                totalSize -= sizes.remove(oldest);
                System.out.println("   вытеснен " + oldest);
            }
        }

        public long getTotalSize() { return totalSize; }

        public int count() { return order.size(); }
    }

    public static void main(String[] args) {
        System.out.println("=== Блок 2 ===");

        List<NewsItem> oldNews = List.of(
                new NewsItem(1, "Обновление 2.0", "..."),
                new NewsItem(2, "Скидки", "до 30%"),
                new NewsItem(3, "Вакансии", "..."));
        List<NewsItem> newNews = List.of(
                new NewsItem(1, "Обновление 2.0", "..."),
                new NewsItem(2, "Скидки", "до 50%"),
                new NewsItem(4, "Новый магазин", "..."));
        System.out.println("11) " + diff(oldNews, newNews));

        List<Integer> feed = new ArrayList<>();
        for (int i = 1; i <= 45; i++) feed.add(i);
        PaginationHelper<Integer> pager = new PaginationHelper<>(feed);
        System.out.println("12) страниц: " + pager.pageCount() + ", страница 3: " + pager.getPage(3)
                + ", страница 4: " + pager.getPage(4));

        System.out.println("13) " + groupByLetter(List.of("Ольга", "антон", "Евгений", "Алина", "Егор", "Олег")));

        List<CatalogItem> catalog = List.of(
                new CatalogItem("SM-A55", "Смартфон Samsung Galaxy A55", 34990),
                new CatalogItem("XM-RN13", "Смартфон Xiaomi Redmi Note 13", 21990),
                new CatalogItem("CASE-A55", "Чехол для Galaxy A55", 790));
        System.out.println("14) 'a55' -> " + filter(catalog, "a55").stream().map(CatalogItem::sku).toList());
        System.out.println("    'XIAOMI' -> " + filter(catalog, "XIAOMI").stream().map(CatalogItem::sku).toList());

        int idx = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(idx).append(' ');
            idx = nextBanner(idx, 4);
        }
        System.out.println("15) индексы баннеров (4 шт): " + sb.toString().trim());

        List<CartPosition> cart = List.of(
                new CartPosition("Кроссовки", "shoes", 5990, 1),
                new CartPosition("Носки", "clothes", 290, 3),
                new CartPosition("Кепка", "clothes", 990, 1));
        System.out.println("16) без скидок: " + cartTotal(cart, Map.of())
                + ", со скидкой shoes -15%: " + cartTotal(cart, Map.of("shoes", 15)));

        System.out.println("17) Свайп-удаление:");
        UndoableList<String> mails = new UndoableList<>(List.of("Письмо 1", "Письмо 2", "Письмо 3"));
        mails.swipeDelete(1, 0);
        System.out.println("   после свайпа: " + mails.getItems());
        System.out.println("   undo через 2 с: " + mails.undo(2000) + " -> " + mails.getItems());
        mails.swipeDelete(0, 10_000);
        System.out.println("   undo через 5 с: " + mails.undo(15_000));
        mails.commit(15_000);
        System.out.println("   итог: " + mails.getItems());

        List<Dialog> dialogs = new ArrayList<>(List.of(
                new Dialog("Мама", 1_790_000_100L),
                new Dialog("Группа П24", 1_790_000_900L),
                new Dialog("Курьер", 1_790_000_500L)));
        sortDialogs(dialogs);
        System.out.println("18) " + dialogs.stream().map(Dialog::title).toList());

        List<MediaFile> gallery = List.of(
                new MediaFile("/DCIM/Camera/IMG_001.jpg", "IMG_001.jpg", 3_200_000),
                new MediaFile("/Download/IMG_001.jpg", "IMG_001.jpg", 3_200_000),
                new MediaFile("/DCIM/Camera/IMG_002.jpg", "IMG_002.jpg", 2_900_000),
                new MediaFile("/Telegram/IMG_002.jpg", "IMG_002.jpg", 1_100_000));
        System.out.println("19) дубликаты:");
        for (List<MediaFile> group : findDuplicates(gallery)) {
            System.out.println("   " + group.stream().map(MediaFile::path).toList());
        }

        System.out.println("20) Кэш картинок:");
        ImageCache images = new ImageCache();
        for (int i = 1; i <= 6; i++) {
            images.put("img_" + i + ".webp", 20L * 1024 * 1024);
        }
        System.out.println("   файлов: " + images.count() + ", объем: " + images.getTotalSize() / (1024 * 1024) + " МБ");
    }
}
