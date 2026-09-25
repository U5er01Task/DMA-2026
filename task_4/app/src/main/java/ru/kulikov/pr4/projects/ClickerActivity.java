package ru.kulikov.pr4.projects;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Проект 1: Тап-кликер. Счет сохраняется при повороте экрана.
 */
public class ClickerActivity extends AppCompatActivity {

    private static final String STATE_SCORE = "score";

    private int score = 0;
    private TextView tvCounter;
    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicker);

        tvCounter = findViewById(R.id.tvCounter);
        rootLayout = findViewById(R.id.rootLayout);
        Button btnClickMe = findViewById(R.id.btnClickMe);
        Button btnReset = findViewById(R.id.btnReset);

        if (savedInstanceState != null) {
            score = savedInstanceState.getInt(STATE_SCORE);
        }
        updateScreen();

        btnClickMe.setOnClickListener(v -> {
            score++;
            updateScreen();
            if (score == 10) {
                Toast.makeText(this, R.string.clicker_10, Toast.LENGTH_SHORT).show();
            } else if (score == 50) {
                Toast.makeText(this, R.string.clicker_50, Toast.LENGTH_SHORT).show();
            }
        });

        btnReset.setOnClickListener(v -> {
            score = 0;
            updateScreen();
            Toast.makeText(this, R.string.clicker_reset_done, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateScreen() {
        tvCounter.setText(String.valueOf(score));
        int color;
        if (score >= 50) {
            color = R.color.clicker_gold;
        } else if (score >= 10) {
            color = R.color.clicker_green;
        } else {
            color = R.color.clicker_default;
        }
        rootLayout.setBackgroundColor(getColor(color));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SCORE, score);
    }
}
