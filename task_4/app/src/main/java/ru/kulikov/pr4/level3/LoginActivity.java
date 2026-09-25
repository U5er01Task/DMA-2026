package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 21. Экран входа (учебный хардкод admin / 1234).
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_LOGIN = "EXTRA_LOGIN";

    // Только для учебного задания! В реальном приложении пароль проверяет сервер.
    private static final String TEST_LOGIN = "admin";
    private static final String TEST_PASSWORD = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText editLogin = findViewById(R.id.editLogin);
        EditText editPassword = findViewById(R.id.editPassword);
        TextView textError = findViewById(R.id.textError);

        findViewById(R.id.buttonLogin).setOnClickListener(v -> {
            String login = editLogin.getText().toString().trim();
            String password = editPassword.getText().toString();

            if (TEST_LOGIN.equals(login) && TEST_PASSWORD.equals(password)) {
                textError.setText("");
                editPassword.setText("");
                Intent intent = new Intent(this, CabinetActivity.class);
                intent.putExtra(EXTRA_LOGIN, login);
                startActivity(intent);
            } else {
                textError.setText(R.string.login_error);
            }
        });
    }
}
