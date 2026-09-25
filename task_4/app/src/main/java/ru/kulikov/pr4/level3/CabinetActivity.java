package ru.kulikov.pr4.level3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ru.kulikov.pr4.R;

/**
 * Задание 21. Личный кабинет после входа.
 */
public class CabinetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet);

        TextView textWelcome = findViewById(R.id.textWelcome);
        TextView textInfo = findViewById(R.id.textInfo);

        String login = getIntent().getStringExtra(LoginActivity.EXTRA_LOGIN);
        textWelcome.setText(getString(R.string.cabinet_welcome, login));
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        textInfo.setText(getString(R.string.cabinet_info, time));

        findViewById(R.id.buttonLogout).setOnClickListener(v -> finish());
    }
}
