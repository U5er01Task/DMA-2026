package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 4. Кнопка по очереди показывает и скрывает текст.
 */
public class ToggleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toggle);

        Button buttonToggle = findViewById(R.id.buttonToggle);
        TextView textSecret = findViewById(R.id.textSecret);

        buttonToggle.setOnClickListener(v -> {
            boolean visible = textSecret.getVisibility() == View.VISIBLE;
            textSecret.setVisibility(visible ? View.GONE : View.VISIBLE);
            buttonToggle.setText(visible ? R.string.toggle_show : R.string.toggle_hide);
        });
    }
}
