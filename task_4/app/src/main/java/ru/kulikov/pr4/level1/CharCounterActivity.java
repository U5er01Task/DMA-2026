package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 7. Счетчик символов в реальном времени (TextWatcher).
 */
public class CharCounterActivity extends AppCompatActivity {

    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_char_counter);

        EditText editInput = findViewById(R.id.editInput);
        textResult = findViewById(R.id.textResult);
        showCount("");

        editInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showCount(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showCount(CharSequence text) {
        int letters = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                letters++;
            }
        }
        textResult.setText(getString(R.string.chars_count, text.length(), letters));
    }
}
