package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 9. Перевод сантиметров в дюймы (1 дюйм = 2.54 см).
 */
public class CmToInchActivity extends AppCompatActivity {

    private static final double CM_IN_INCH = 2.54;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cm_to_inch);

        EditText editInput = findViewById(R.id.editInput);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            String text = editInput.getText().toString().trim().replace(',', '.');
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double cm = Double.parseDouble(text);
                textResult.setText(getString(R.string.inches_result, cm, cm / CM_IN_INCH));
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_number, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
