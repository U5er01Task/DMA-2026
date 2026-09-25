package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import ru.kulikov.pr4.R;

/**
 * Задание 2. Бросок кубика: случайное число от 1 до 6.
 */
public class DiceActivity extends AppCompatActivity {

    private final Random random = new Random();
    private int rolls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        TextView textDice = findViewById(R.id.textDice);
        TextView textRolls = findViewById(R.id.textRolls);
        textRolls.setText(getString(R.string.dice_rolls, rolls));

        findViewById(R.id.buttonRoll).setOnClickListener(v -> {
            int value = random.nextInt(6) + 1; // nextInt(6) дает 0..5
            rolls++;
            textDice.setText(String.valueOf(value));
            textRolls.setText(getString(R.string.dice_rolls, rolls));
        });
    }
}
