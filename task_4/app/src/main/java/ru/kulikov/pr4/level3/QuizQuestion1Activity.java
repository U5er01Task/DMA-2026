package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 23. Квиз: первый вопрос, баллы передаются на второй экран.
 */
public class QuizQuestion1Activity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "EXTRA_SCORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_question);

        ((TextView) findViewById(R.id.textNumber)).setText(getString(R.string.quiz_q_number, 1));
        ((TextView) findViewById(R.id.textQuestion)).setText(R.string.quiz_q1);
        ((RadioButton) findViewById(R.id.radioA1)).setText(R.string.quiz_q1_a1);
        ((RadioButton) findViewById(R.id.radioA2)).setText(R.string.quiz_q1_a2);
        ((RadioButton) findViewById(R.id.radioA3)).setText(R.string.quiz_q1_a3);

        RadioGroup radioAnswers = findViewById(R.id.radioAnswers);
        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setText(R.string.quiz_next);

        buttonNext.setOnClickListener(v -> {
            int checked = radioAnswers.getCheckedRadioButtonId();
            if (checked == -1) {
                Toast.makeText(this, R.string.quiz_choose, Toast.LENGTH_SHORT).show();
                return;
            }
            int score = checked == R.id.radioA2 ? 1 : 0; // правильный ответ: setContentView()
            Intent intent = new Intent(this, QuizQuestion2Activity.class);
            intent.putExtra(EXTRA_SCORE, score);
            startActivity(intent);
            finish(); // назад на первый вопрос вернуться нельзя
        });
    }
}
