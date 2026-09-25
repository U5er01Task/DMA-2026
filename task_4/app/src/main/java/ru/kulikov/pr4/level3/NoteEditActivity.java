package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 25. Дневник заметок: ввод текста.
 * Экран не закрывается при переходе, поэтому после "Редактировать" текст остается в поле.
 */
public class NoteEditActivity extends AppCompatActivity {

    public static final String EXTRA_TEXT = "EXTRA_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        EditText editNote = findViewById(R.id.editNote);

        findViewById(R.id.buttonRead).setOnClickListener(v -> {
            String text = editNote.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.notes_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, NoteReadActivity.class);
            intent.putExtra(EXTRA_TEXT, text);
            startActivity(intent);
        });
    }
}
