package ru.kulikov.pr4.projects;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Проект 3: Визитка студента. Экран анкеты.
 */
public class StudentFormActivity extends AppCompatActivity {

    public static final String KEY_NAME = "KEY_NAME";
    public static final String KEY_GROUP = "KEY_GROUP";
    public static final String KEY_SKILL = "KEY_SKILL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_form);

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etGroup = findViewById(R.id.etGroup);
        EditText etSkill = findViewById(R.id.etSkill);
        Button btnGenerate = findViewById(R.id.btnGenerateCard);

        btnGenerate.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String group = etGroup.getText().toString().trim();
            String skill = etSkill.getText().toString().trim();

            if (name.isEmpty() || group.isEmpty() || skill.isEmpty()) {
                Toast.makeText(this, R.string.card_error_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(StudentFormActivity.this, StudentCardActivity.class);
            intent.putExtra(KEY_NAME, name);
            intent.putExtra(KEY_GROUP, group);
            intent.putExtra(KEY_SKILL, skill);
            startActivity(intent);
        });
    }
}
