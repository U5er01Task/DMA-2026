package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 30. Итоговый проект "Портфолио студента": главный экран.
 */
public class PortfolioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        findViewById(R.id.buttonAbout).setOnClickListener(v ->
                startActivity(new Intent(this, AboutMeActivity.class)));
        findViewById(R.id.buttonSkills).setOnClickListener(v ->
                startActivity(new Intent(this, SkillsActivity.class)));
        findViewById(R.id.buttonContacts).setOnClickListener(v ->
                startActivity(new Intent(this, ContactsActivity.class)));
    }
}
