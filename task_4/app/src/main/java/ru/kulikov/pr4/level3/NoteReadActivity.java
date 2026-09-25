package ru.kulikov.pr4.level3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 25. Режим чтения заметки крупным шрифтом.
 */
public class NoteReadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_read);

        ((TextView) findViewById(R.id.textNote)).setText(getIntent().getStringExtra(NoteEditActivity.EXTRA_TEXT));
        // "Редактировать" = вернуться на экран ввода, там текст сохранился
        findViewById(R.id.buttonEdit).setOnClickListener(v -> finish());
    }
}
