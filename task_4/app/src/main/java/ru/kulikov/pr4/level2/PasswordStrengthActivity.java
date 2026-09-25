package ru.kulikov.pr4.level2;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 20. Надежность пароля по длине: <6 слабый, 6-10 средний, >10 надежный.
 */
public class PasswordStrengthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        EditText editInput = findViewById(R.id.editInput);
        ProgressBar progress = findViewById(R.id.progressStrength);
        TextView textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonAction).setOnClickListener(v -> {
            int length = editInput.getText().length();
            int text;
            int color;
            int level;
            if (length < 6) {
                text = R.string.password_weak;
                color = R.color.error_red;
                level = 1;
            } else if (length <= 10) {
                text = R.string.password_medium;
                color = R.color.warning_orange;
                level = 2;
            } else {
                text = R.string.password_strong;
                color = R.color.brand_green;
                level = 3;
            }
            textResult.setText(text);
            textResult.setTextColor(getColor(color));
            progress.setProgress(level);
            progress.setProgressTintList(ColorStateList.valueOf(getColor(color)));
        });
    }
}
