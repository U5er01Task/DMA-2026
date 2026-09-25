package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 12. Средний расход = литры / км * 100.
 */
public class FuelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel);

        EditText editDistance = findViewById(R.id.editFirst);
        EditText editLiters = findViewById(R.id.editSecond);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            Double km = InputUtils.readDouble(editDistance);
            Double liters = InputUtils.readDouble(editLiters);
            if (km == null || liters == null || km <= 0 || liters < 0) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            textResult.setText(getString(R.string.fuel_result, liters / km * 100));
        });
    }
}
