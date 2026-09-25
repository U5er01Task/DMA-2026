package ru.kulikov.pr4.basics;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Раздел 6, задание 1: третий экран с информацией об авторе.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textViewAgreed = findViewById(R.id.textViewAgreed);
        Button buttonExit = findViewById(R.id.buttonExit);

        boolean agreed = getIntent().getBooleanExtra(SecondActivity.EXTRA_AGREED, false);
        textViewAgreed.setText(agreed ? R.string.about_agreed : R.string.about_not_agreed);
        textViewAgreed.setTextColor(getColor(agreed ? R.color.brand_green : R.color.error_red));

        buttonExit.setOnClickListener(v -> finish());
    }
}
