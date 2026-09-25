package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 28. Спортивный трекер: цель и пройденные шаги.
 */
public class StepsFormActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL = "EXTRA_GOAL";
    public static final String EXTRA_DONE = "EXTRA_DONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_form);

        EditText editGoal = findViewById(R.id.editGoal);
        EditText editDone = findViewById(R.id.editDone);

        findViewById(R.id.buttonShow).setOnClickListener(v -> {
            Integer goal = InputUtils.readInt(editGoal);
            Integer done = InputUtils.readInt(editDone);
            if (goal == null || done == null || goal <= 0 || done < 0) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, StepsProgressActivity.class);
            intent.putExtra(EXTRA_GOAL, goal);
            intent.putExtra(EXTRA_DONE, done);
            startActivity(intent);
        });
    }
}
