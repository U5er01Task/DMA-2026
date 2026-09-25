package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 18. Тест на знание столиц: страна + 3 варианта, подсчет очков.
 */
public class CapitalsQuizActivity extends AppCompatActivity {

    private static final int OPTIONS_PER_QUESTION = 3;

    // вопросы лежат в res/values/strings_level2.xml, чтобы их можно было перевести
    private String[] countries;
    private String[] correctAnswers;
    private String[] options;

    private TextView textQuestion;
    private TextView textScore;
    private TextView textFeedback;
    private Button buttonRestart;
    private Button[] answerButtons;

    private int current = 0;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capitals);

        textQuestion = findViewById(R.id.textQuestion);
        textScore = findViewById(R.id.textScore);
        textFeedback = findViewById(R.id.textFeedback);
        buttonRestart = findViewById(R.id.buttonRestart);
        countries = getResources().getStringArray(R.array.capitals_countries);
        correctAnswers = getResources().getStringArray(R.array.capitals_correct);
        options = getResources().getStringArray(R.array.capitals_options);

        answerButtons = new Button[]{
                findViewById(R.id.buttonAnswer1),
                findViewById(R.id.buttonAnswer2),
                findViewById(R.id.buttonAnswer3)
        };

        for (Button button : answerButtons) {
            button.setOnClickListener(v -> checkAnswer(((Button) v).getText().toString()));
        }
        buttonRestart.setOnClickListener(v -> {
            current = 0;
            score = 0;
            textFeedback.setText("");
            buttonRestart.setVisibility(View.GONE);
            showQuestion();
        });

        showQuestion();
    }

    private void showQuestion() {
        textQuestion.setText(getString(R.string.capitals_question, countries[current]));
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(options[current * OPTIONS_PER_QUESTION + i]);
            answerButtons[i].setEnabled(true);
        }
        textScore.setText(getString(R.string.capitals_score, score, countries.length));
    }

    private void checkAnswer(String answer) {
        String correct = correctAnswers[current];
        if (answer.equals(correct)) {
            score++;
            textFeedback.setText(R.string.capitals_right);
            textFeedback.setTextColor(getColor(R.color.brand_green));
        } else {
            textFeedback.setText(getString(R.string.capitals_wrong, correct));
            textFeedback.setTextColor(getColor(R.color.error_red));
        }

        current++;
        if (current < countries.length) {
            showQuestion();
        } else {
            textScore.setText(getString(R.string.capitals_score, score, countries.length));
            textQuestion.setText(getString(R.string.capitals_finish, score, countries.length));
            for (Button button : answerButtons) {
                button.setEnabled(false);
            }
            buttonRestart.setVisibility(View.VISIBLE);
        }
    }
}
