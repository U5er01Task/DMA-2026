package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 22. Выбор размера пиццы и адреса доставки.
 */
public class PizzaOrderActivity extends AppCompatActivity {

    public static final String EXTRA_SIZE = "EXTRA_SIZE";
    public static final String EXTRA_PRICE = "EXTRA_PRICE";
    public static final String EXTRA_COUNT = "EXTRA_COUNT";
    public static final String EXTRA_CHEESE = "EXTRA_CHEESE";
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_order);

        RadioGroup radioSize = findViewById(R.id.radioSize);
        CheckBox checkCheese = findViewById(R.id.checkCheese);
        EditText editCount = findViewById(R.id.editCount);
        EditText editAddress = findViewById(R.id.editAddress);

        findViewById(R.id.buttonOrder).setOnClickListener(v -> {
            int size;
            int price;
            int checked = radioSize.getCheckedRadioButtonId();
            if (checked == R.id.radioSmall) {
                size = 25;
                price = 450;
            } else if (checked == R.id.radioMedium) {
                size = 30;
                price = 650;
            } else if (checked == R.id.radioLarge) {
                size = 35;
                price = 850;
            } else {
                Toast.makeText(this, R.string.pizza_error_size, Toast.LENGTH_SHORT).show();
                return;
            }

            Integer count = InputUtils.readInt(editCount);
            String address = editAddress.getText().toString().trim();
            if (count == null || count < 1 || address.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, PizzaReceiptActivity.class);
            intent.putExtra(EXTRA_SIZE, size);
            intent.putExtra(EXTRA_PRICE, price);
            intent.putExtra(EXTRA_COUNT, count);
            intent.putExtra(EXTRA_CHEESE, checkCheese.isChecked());
            intent.putExtra(EXTRA_ADDRESS, address);
            startActivity(intent);
        });
    }
}
