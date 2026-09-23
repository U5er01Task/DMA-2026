package pr2.topics;

import java.util.List;

/**
 * Тема 6. Наследование и полиморфизм.
 */
public class Topic6Inheritance {

    // 1. Иерархия экранов
    public static class BaseScreen {
        protected final String name;

        public BaseScreen(String name) {
            this.name = name;
        }

        public void onOpen() {
            System.out.println("   [" + name + "] открыт");
        }

        public void onClose() {
            System.out.println("   [" + name + "] закрыт");
        }
    }

    public static class LoginScreen extends BaseScreen {
        public LoginScreen() { super("LoginScreen"); }

        @Override
        public void onOpen() {
            super.onOpen();
            System.out.println("   показываем поля логина и пароля");
        }
    }

    public static class HomeScreen extends BaseScreen {
        public HomeScreen() { super("HomeScreen"); }

        @Override
        public void onOpen() {
            super.onOpen();
            System.out.println("   загружаем ленту");
        }
    }

    public static class SettingsScreen extends BaseScreen {
        public SettingsScreen() { super("SettingsScreen"); }

        @Override
        public void onClose() {
            System.out.println("   сохраняем настройки");
            super.onClose();
        }
    }

    // 2. События аналитики
    public abstract static class AnalyticsEvent {
        protected final long timestamp;

        protected AnalyticsEvent(long timestamp) {
            this.timestamp = timestamp;
        }

        public abstract String describe();
    }

    public static class ClickEvent extends AnalyticsEvent {
        private final String buttonId;

        public ClickEvent(long ts, String buttonId) {
            super(ts);
            this.buttonId = buttonId;
        }

        @Override
        public String describe() { return "CLICK button=" + buttonId; }
    }

    public static class PurchaseEvent extends AnalyticsEvent {
        private final double amount;

        public PurchaseEvent(long ts, double amount) {
            super(ts);
            this.amount = amount;
        }

        @Override
        public String describe() { return "PURCHASE amount=" + amount; }
    }

    public static class ScreenViewEvent extends AnalyticsEvent {
        private final String screen;

        public ScreenViewEvent(long ts, String screen) {
            super(ts);
            this.screen = screen;
        }

        @Override
        public String describe() { return "SCREEN_VIEW screen=" + screen; }
    }

    public static class AnalyticsService {
        public void log(AnalyticsEvent event) {
            System.out.println("   [analytics t=" + event.timestamp + "] " + event.describe());
        }
    }

    // 3. Сенсоры
    public static class DeviceSensor {
        public String readData() { return "нет данных"; }
    }

    public static class GyroscopeSensor extends DeviceSensor {
        @Override
        public String readData() { return "гироскоп: x=0.02 y=-0.15 z=0.01 рад/с"; }
    }

    public static class LightSensor extends DeviceSensor {
        @Override
        public String readData() { return "освещенность: 320 лк"; }
    }

    // 4. Отрисовка экрана из разных компонентов
    public abstract static class UiComponent {
        protected final int id;

        protected UiComponent(int id) { this.id = id; }

        public abstract void render();
    }

    public static class ButtonComponent extends UiComponent {
        private final String text;

        public ButtonComponent(int id, String text) {
            super(id);
            this.text = text;
        }

        @Override
        public void render() { System.out.println("   Кнопка [" + id + "]: '" + text + "'"); }
    }

    public static class ImageComponent extends UiComponent {
        private final String url;

        public ImageComponent(int id, String url) {
            super(id);
            this.url = url;
        }

        @Override
        public void render() { System.out.println("   Картинка [" + id + "]: " + url); }
    }

    public static class TextComponent extends UiComponent {
        private final String text;

        public TextComponent(int id, String text) {
            super(id);
            this.text = text;
        }

        @Override
        public void render() { System.out.println("   Текст [" + id + "]: " + text); }
    }

    public static void drawScreen(List<UiComponent> components) {
        for (UiComponent component : components) {
            component.render();
        }
    }

    // 5. Подписки
    public static class Subscription {
        protected final double basePrice;

        public Subscription(double basePrice) { this.basePrice = basePrice; }

        public double monthlyCost() { return basePrice; }

        public String name() { return "Базовая"; }
    }

    public static class MonthlySubscription extends Subscription {
        public MonthlySubscription(double basePrice) { super(basePrice); }

        @Override
        public String name() { return "Месячная"; }
    }

    public static class FamilySubscription extends Subscription {
        private final int users;

        public FamilySubscription(double basePrice, int users) {
            super(basePrice);
            this.users = users;
        }

        // каждый дополнительный пользователь стоит 40% базы
        @Override
        public double monthlyCost() { return basePrice + basePrice * 0.4 * (users - 1); }

        @Override
        public String name() { return "Семейная на " + users; }
    }

    public static class AnnualDiscountSubscription extends Subscription {
        public AnnualDiscountSubscription(double basePrice) { super(basePrice); }

        // при оплате за год скидка 20%
        @Override
        public double monthlyCost() { return basePrice * 0.8; }

        @Override
        public String name() { return "Годовая"; }
    }

    public static void main(String[] args) {
        System.out.println("=== Тема 6 ===");

        System.out.println("1) Экраны:");
        List<BaseScreen> screens = List.of(new LoginScreen(), new HomeScreen(), new SettingsScreen());
        for (BaseScreen screen : screens) {
            screen.onOpen();
            screen.onClose();
        }

        System.out.println("2) Аналитика:");
        AnalyticsService analytics = new AnalyticsService();
        analytics.log(new ClickEvent(1001, "btn_buy"));
        analytics.log(new PurchaseEvent(1002, 2990.0));
        analytics.log(new ScreenViewEvent(1003, "Cart"));

        System.out.println("3) Сенсоры:");
        List<DeviceSensor> sensors = List.of(new GyroscopeSensor(), new LightSensor());
        for (DeviceSensor sensor : sensors) {
            System.out.println("   " + sensor.readData());
        }

        System.out.println("4) drawScreen:");
        drawScreen(List.of(
                new TextComponent(1, "Добро пожаловать"),
                new ImageComponent(2, "https://cdn.example.com/banner.png"),
                new ButtonComponent(3, "Войти")
        ));

        System.out.println("5) Подписки (база 299 руб.):");
        List<Subscription> plans = List.of(
                new MonthlySubscription(299),
                new FamilySubscription(299, 4),
                new AnnualDiscountSubscription(299)
        );
        for (Subscription plan : plans) {
            System.out.printf("   %s: %.2f руб./мес%n", plan.name(), plan.monthlyCost());
        }
    }
}
