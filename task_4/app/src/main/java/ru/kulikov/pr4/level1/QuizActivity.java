package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 3. Мини-тест: вопрос, поле ответа и проверка.
 */
public class QuizActivity extends AppCompatActivity {

    private static final int CORRECT_ANSWER = 56;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        EditText editInput = findViewById(R.id.editInput);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            String answer = editInput.getText().toString().trim();
            if (answer.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean right = answer.equals(String.valueOf(CORRECT_ANSWER));
            textResult.setText(right ? R.string.quiz_right : R.string.quiz_wrong);
            textResult.setTextColor(getColor(right ? R.color.brand_green : R.color.error_red));
        });
    }
}
