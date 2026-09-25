package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 19. Время в пути = расстояние / скорость, выводим в часах и минутах.
 */
public class TravelTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_time);

        EditText editDistance = findViewById(R.id.editFirst);
        EditText editSpeed = findViewById(R.id.editSecond);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            Double distance = InputUtils.readDouble(editDistance);
            Double speed = InputUtils.readDouble(editSpeed);
            if (distance == null || speed == null || distance < 0 || speed <= 0) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            long totalMinutes = Math.round(distance / speed * 60);
            textResult.setText(getString(R.string.travel_result, totalMinutes / 60, totalMinutes % 60));
        });
    }
}
