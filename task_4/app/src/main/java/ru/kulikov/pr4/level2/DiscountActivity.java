package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 15. Сумма скидки и итоговая цена.
 */
public class DiscountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount);

        EditText editPrice = findViewById(R.id.editFirst);
        EditText editPercent = findViewById(R.id.editSecond);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            Double price = InputUtils.readDouble(editPrice);
            Double percent = InputUtils.readDouble(editPercent);
            if (price == null || percent == null || price < 0) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (percent < 0 || percent > 100) {
                Toast.makeText(this, R.string.discount_error, Toast.LENGTH_SHORT).show();
                return;
            }
            double discount = price * percent / 100;
            textResult.setText(getString(R.string.discount_result, discount, price - discount));
        });
    }
}
