package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 29. Мини-словарь: три термина, по клику - экран с описанием.
 */
public class GlossaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glossary);

        findViewById(R.id.buttonActivity).setOnClickListener(v ->
                openTerm(R.string.term_activity, R.string.term_activity_text));
        findViewById(R.id.buttonIntent).setOnClickListener(v ->
                openTerm(R.string.term_intent, R.string.term_intent_text));
        findViewById(R.id.buttonLayout).setOnClickListener(v ->
                openTerm(R.string.term_layout, R.string.term_layout_text));
    }

    private void openTerm(int titleRes, int textRes) {
        Intent intent = new Intent(this, TermActivity.class);
        // передаем id ресурсов, а не сами строки - так перевод подхватится на втором экране
        intent.putExtra(TermActivity.EXTRA_TITLE, titleRes);
        intent.putExtra(TermActivity.EXTRA_TEXT, textRes);
        startActivity(intent);
    }
}
