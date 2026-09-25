package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 22. Чек заказа с итоговой стоимостью.
 */
public class PizzaReceiptActivity extends AppCompatActivity {

    private static final int CHEESE_PRICE = 90;
    private static final int DELIVERY_PRICE = 150;
    private static final int FREE_DELIVERY_FROM = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_receipt);

        Intent intent = getIntent();
        int size = intent.getIntExtra(PizzaOrderActivity.EXTRA_SIZE, 0);
        int price = intent.getIntExtra(PizzaOrderActivity.EXTRA_PRICE, 0);
        int count = intent.getIntExtra(PizzaOrderActivity.EXTRA_COUNT, 1);
        boolean cheese = intent.getBooleanExtra(PizzaOrderActivity.EXTRA_CHEESE, false);
        String address = intent.getStringExtra(PizzaOrderActivity.EXTRA_ADDRESS);

        int pizzaSum = (price + (cheese ? CHEESE_PRICE : 0)) * count;
        int delivery = pizzaSum >= FREE_DELIVERY_FROM ? 0 : DELIVERY_PRICE;

        TextView textReceipt = findViewById(R.id.textReceipt);
        TextView textTotal = findViewById(R.id.textTotal);

        String deliveryText = delivery == 0
                ? getString(R.string.receipt_free_delivery)
                : getString(R.string.receipt_delivery_price, delivery);
        textReceipt.setText(getString(R.string.receipt_body, size, count, pizzaSum,
                getString(cheese ? R.string.yes : R.string.no), deliveryText, address));
        textTotal.setText(getString(R.string.receipt_total, pizzaSum + delivery));

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }
}
