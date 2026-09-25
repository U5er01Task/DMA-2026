package ru.kulikov.pr4.level3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 30. Портфолио: экран "Мои навыки".
 */
public class SkillsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_detail);

        ((TextView) findViewById(R.id.textTitle)).setText(R.string.portfolio_skills);
        ((TextView) findViewById(R.id.textBody)).setText(R.string.skills_text);
    }
}
