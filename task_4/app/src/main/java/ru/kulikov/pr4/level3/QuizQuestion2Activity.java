package ru.kulikov.pr4.level3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 23. Квиз: второй вопрос и итоговый балл.
 */
public class QuizQuestion2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_question);

        int scoreFromFirst = getIntent().getIntExtra(QuizQuestion1Activity.EXTRA_SCORE, 0);

        ((TextView) findViewById(R.id.textNumber)).setText(getString(R.string.quiz_q_number, 2));
        ((TextView) findViewById(R.id.textQuestion)).setText(R.string.quiz_q2);
        ((RadioButton) findViewById(R.id.radioA1)).setText(R.string.quiz_q2_a1);
        ((RadioButton) findViewById(R.id.radioA2)).setText(R.string.quiz_q2_a2);
        ((RadioButton) findViewById(R.id.radioA3)).setText(R.string.quiz_q2_a3);

        RadioGroup radioAnswers = findViewById(R.id.radioAnswers);
        TextView textTotal = findViewById(R.id.textTotal);
        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setText(R.string.quiz_finish);

        buttonNext.setOnClickListener(v -> {
            int checked = radioAnswers.getCheckedRadioButtonId();
            if (checked == -1) {
                Toast.makeText(this, R.string.quiz_choose, Toast.LENGTH_SHORT).show();
                return;
            }
            int total = scoreFromFirst + (checked == R.id.radioA3 ? 1 : 0); // правильный ответ: sp
            textTotal.setText(getString(R.string.quiz_total, total));
            // после ответа выбор блокируем
            for (int i = 0; i < radioAnswers.getChildCount(); i++) {
                radioAnswers.getChildAt(i).setEnabled(false);
            }
            buttonNext.setVisibility(View.GONE);
        });
    }
}
