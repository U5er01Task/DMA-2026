package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 10. Четное или нечетное число.
 */
public class ParityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parity);

        EditText editInput = findViewById(R.id.editInput);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            String text = editInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                long number = Long.parseLong(text);
                // % 2 у отрицательных нечетных дает -1, поэтому сравниваем с 0
                boolean even = number % 2 == 0;
                textResult.setText(getString(even ? R.string.parity_even : R.string.parity_odd, number));
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_number, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
