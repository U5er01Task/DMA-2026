package ru.kulikov.pr4.level3;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 28. Прогресс-бар и процент выполнения нормы шагов.
 */
public class StepsProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_progress);

        int goal = getIntent().getIntExtra(StepsFormActivity.EXTRA_GOAL, 1);
        int done = getIntent().getIntExtra(StepsFormActivity.EXTRA_DONE, 0);
        int percent = (int) Math.round(done * 100.0 / goal);

        ((TextView) findViewById(R.id.textPercent)).setText(getString(R.string.steps_percent, percent));
        ((ProgressBar) findViewById(R.id.progressSteps)).setProgress(Math.min(percent, 100));
        ((TextView) findViewById(R.id.textDetails)).setText(
                getResources().getQuantityString(R.plurals.steps_details, goal, done, goal));

        TextView textStatus = findViewById(R.id.textStatus);
        if (done >= goal) {
            textStatus.setText(R.string.steps_done);
        } else {
            textStatus.setText(getResources().getQuantityString(R.plurals.steps_left, goal - done, goal - done));
        }
    }
}
