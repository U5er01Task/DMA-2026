package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 6. Возраст собаки: человеческие годы * 7.
 */
public class PetAgeActivity extends AppCompatActivity {

    private static final int DOG_YEARS_FACTOR = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_age);

        EditText editInput = findViewById(R.id.editInput);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            String text = editInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int years = Integer.parseInt(text);
                int dogYears = years * DOG_YEARS_FACTOR;
                // plurals: "1 год", "2 года", "5 лет"
                textResult.setText(getResources().getQuantityString(R.plurals.pet_result, dogYears, dogYears));
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_number, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
