package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 13. Цельсий -> Фаренгейт (C * 1.8 + 32) и Кельвин (C + 273.15).
 */
public class TemperatureActivity extends AppCompatActivity {

    private static final double ABSOLUTE_ZERO_C = -273.15;

    private EditText editInput;
    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        editInput = findViewById(R.id.editInput);
        textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonToF).setOnClickListener(v -> convert(false));
        findViewById(R.id.buttonToK).setOnClickListener(v -> convert(true));
    }

    private void convert(boolean toKelvin) {
        Double celsius = InputUtils.readDouble(editInput);
        if (celsius == null) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (celsius < ABSOLUTE_ZERO_C) {
            textResult.setText(R.string.temp_error_k);
            return;
        }
        if (toKelvin) {
            textResult.setText(getString(R.string.temp_result_k, celsius, celsius - ABSOLUTE_ZERO_C));
        } else {
            textResult.setText(getString(R.string.temp_result_f, celsius, celsius * 1.8 + 32));
        }
    }
}
