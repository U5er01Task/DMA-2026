package ru.kulikov.pr4.level3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 29. Подробное описание термина.
 */
public class TermActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_TEXT = "EXTRA_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term);

        int titleRes = getIntent().getIntExtra(EXTRA_TITLE, R.string.l3_glossary);
        int textRes = getIntent().getIntExtra(EXTRA_TEXT, R.string.l3_glossary);

        ((TextView) findViewById(R.id.textTitle)).setText(titleRes);
        ((TextView) findViewById(R.id.textDescription)).setText(textRes);
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }
}
