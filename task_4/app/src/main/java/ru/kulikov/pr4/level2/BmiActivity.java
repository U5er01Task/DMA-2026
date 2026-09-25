package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 11. ИМТ = вес / (рост в метрах)^2.
 */
public class BmiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        EditText editWeight = findViewById(R.id.editFirst);
        EditText editHeight = findViewById(R.id.editSecond);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            Double weight = InputUtils.readDouble(editWeight);
            Double heightCm = InputUtils.readDouble(editHeight);
            if (weight == null || heightCm == null || weight <= 0 || heightCm <= 0) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            double heightM = heightCm / 100;
            double bmi = weight / (heightM * heightM);

            int verdict;
            int color;
            if (bmi < 18.5) {
                verdict = R.string.bmi_low;
                color = R.color.brand_blue;
            } else if (bmi < 25) {
                verdict = R.string.bmi_normal;
                color = R.color.brand_green;
            } else {
                verdict = R.string.bmi_high;
                color = R.color.error_red;
            }
            textResult.setText(getString(R.string.bmi_result, bmi, getString(verdict)));
            textResult.setTextColor(getColor(color));
        });
    }
}
