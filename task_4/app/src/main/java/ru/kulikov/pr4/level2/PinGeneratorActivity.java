package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;

import ru.kulikov.pr4.R;

/**
 * Задание 17. PIN из 4 или 6 цифр без одинаковых цифр подряд.
 */
public class PinGeneratorActivity extends AppCompatActivity {

    // SecureRandom, а не Random: PIN должен быть непредсказуемым
    private final SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        TextView textPin = findViewById(R.id.textPin);
        findViewById(R.id.buttonPin4).setOnClickListener(v -> textPin.setText(generatePin(4)));
        findViewById(R.id.buttonPin6).setOnClickListener(v -> textPin.setText(generatePin(6)));
    }

    private String generatePin(int length) {
        StringBuilder pin = new StringBuilder();
        int previous = -1;
        while (pin.length() < length) {
            int digit = random.nextInt(10);
            if (digit == previous) {
                continue; // такая же цифра, как предыдущая - берем другую
            }
            pin.append(digit);
            previous = digit;
        }
        return pin.toString();
    }
}
