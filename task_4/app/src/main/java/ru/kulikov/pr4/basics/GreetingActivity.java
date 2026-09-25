package ru.kulikov.pr4.basics;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Разделы 3-5: первый экран. Приветствие по имени + задания для закрепления.
 */
public class GreetingActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "EXTRA_USERNAME";
    public static final String EXTRA_AGE = "EXTRA_AGE";
    public static final String EXTRA_BALANCE = "EXTRA_BALANCE";

    private TextView textViewTitle;
    private EditText editTextName;
    private EditText editTextAge;
    private TextView textViewResult;
    private TextView textViewCounter;
    private Button buttonToggleResult;

    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // сначала загружаем разметку, только потом findViewById
        setContentView(R.layout.activity_greeting);

        textViewTitle = findViewById(R.id.textViewTitle);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        textViewResult = findViewById(R.id.textViewResult);
        textViewCounter = findViewById(R.id.textViewCounter);
        buttonToggleResult = findViewById(R.id.buttonToggleResult);

        Button buttonGreet = findViewById(R.id.buttonGreet);
        Button buttonClear = findViewById(R.id.buttonClear);
        Button buttonChangeTitle = findViewById(R.id.buttonChangeTitle);
        Button buttonCounter = findViewById(R.id.buttonCounter);
        Button buttonNext = findViewById(R.id.buttonNext);

        textViewCounter.setText(getString(R.string.counter_value, counter));

        buttonGreet.setOnClickListener(v -> greet());

        buttonClear.setOnClickListener(v -> {
            editTextName.setText("");
            editTextAge.setText("");
            textViewResult.setText(R.string.result_placeholder);
            textViewTitle.setText(R.string.title_welcome);
            Toast.makeText(this, R.string.toast_cleared, Toast.LENGTH_SHORT).show();
        });

        // Раздел 4, задание 3
        buttonChangeTitle.setOnClickListener(v -> textViewTitle.setText(R.string.title_changed));

        // Раздел 4, задание 4: скрываем/показываем результат
        buttonToggleResult.setOnClickListener(v -> {
            if (textViewResult.getVisibility() == View.VISIBLE) {
                textViewResult.setVisibility(View.GONE);
                buttonToggleResult.setText(R.string.btn_show_result);
            } else {
                textViewResult.setVisibility(View.VISIBLE);
                buttonToggleResult.setText(R.string.btn_hide_result);
            }
        });

        // Раздел 4, задание 5: счетчик
        buttonCounter.setOnClickListener(v -> {
            counter++;
            textViewCounter.setText(getString(R.string.counter_value, counter));
        });

        // Раздел 6: переход на второй экран с передачей данных
        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(GreetingActivity.this, SecondActivity.class);
            String name = editTextName.getText().toString().trim();
            if (!name.isEmpty()) {
                intent.putExtra(EXTRA_USERNAME, name); // если имя пустое - не передаем, будет "Гость"
            }
            intent.putExtra(EXTRA_AGE, parseAge());
            intent.putExtra(EXTRA_BALANCE, 1520.75); // раздел 6, задание 4: double
            startActivity(intent);
        });
    }

    private void greet() {
        String enteredName = editTextName.getText().toString().trim();

        if (enteredName.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            return;
        }
        // Раздел 4, задание 2
        if (enteredName.length() < 2) {
            editTextName.setError(getString(R.string.error_short_name));
            return;
        }

        int age = parseAge();
        String greeting = age > 0
                ? getString(R.string.welcome_user_age, enteredName, age)
                : getString(R.string.welcome_user, enteredName); // раздел 5, задание 4
        textViewResult.setText(greeting);
        textViewResult.setVisibility(View.VISIBLE);
        buttonToggleResult.setText(R.string.btn_hide_result);

        // Раздел 4, задание 1
        Toast.makeText(this, R.string.toast_greeting_done, Toast.LENGTH_SHORT).show();
    }

    private int parseAge() {
        String ageText = editTextAge.getText().toString().trim();
        if (ageText.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
