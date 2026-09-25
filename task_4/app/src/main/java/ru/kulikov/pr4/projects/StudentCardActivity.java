package ru.kulikov.pr4.projects;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Проект 3: Визитка студента. Экран карточки.
 */
public class StudentCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_card);

        TextView tvCardName = findViewById(R.id.tvCardName);
        TextView tvCardGroup = findViewById(R.id.tvCardGroup);
        TextView tvCardSkill = findViewById(R.id.tvCardSkill);
        Button btnBack = findViewById(R.id.btnBack);

        tvCardName.setText(getIntent().getStringExtra(StudentFormActivity.KEY_NAME));
        tvCardGroup.setText(getString(R.string.card_group, getIntent().getStringExtra(StudentFormActivity.KEY_GROUP)));
        tvCardSkill.setText(getString(R.string.card_skill, getIntent().getStringExtra(StudentFormActivity.KEY_SKILL)));

        btnBack.setOnClickListener(v -> finish());
    }
}
