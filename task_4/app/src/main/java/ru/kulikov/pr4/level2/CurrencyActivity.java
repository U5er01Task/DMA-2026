package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 16. Рубли в доллары и евро по фиксированному курсу.
 */
public class CurrencyActivity extends AppCompatActivity {

    private static final double USD_RATE = 92.50;
    private static final double EUR_RATE = 100.30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        TextView textRates = findViewById(R.id.textRates);
        EditText editInput = findViewById(R.id.editInput);
        TextView textResult = findViewById(R.id.textResult);

        textRates.setText(getString(R.string.currency_rates, USD_RATE, EUR_RATE));

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            Double rubles = InputUtils.readDouble(editInput);
            if (rubles == null || rubles < 0) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            // %.2f в строке сам округляет до копеек/центов
            textResult.setText(getString(R.string.currency_result, rubles, rubles / USD_RATE, rubles / EUR_RATE));
        });
    }
}
