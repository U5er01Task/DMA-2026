package ru.kulikov.pr4.basics;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Раздел 6: второй экран. Принимает данные из GreetingActivity.
 */
public class SecondActivity extends AppCompatActivity {

    public static final String EXTRA_AGREED = "EXTRA_AGREED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView textViewReceived = findViewById(R.id.textViewReceivedData);
        CheckBox checkBoxAgree = findViewById(R.id.checkBoxAgree);
        Button buttonAbout = findViewById(R.id.buttonAbout);
        Button buttonBrowser = findViewById(R.id.buttonBrowser);
        Button buttonClose = findViewById(R.id.buttonClose);

        Intent incomingIntent = getIntent();
        String userName = incomingIntent.getStringExtra(GreetingActivity.EXTRA_USERNAME);
        int userAge = incomingIntent.getIntExtra(GreetingActivity.EXTRA_AGE, 0);
        double balance = incomingIntent.getDoubleExtra(GreetingActivity.EXTRA_BALANCE, 0.0);

        // Раздел 6, задание 3: если имя не передали - показываем "Гость"
        if (userName == null) {
            userName = getString(R.string.guest);
        }
        textViewReceived.setText(getString(R.string.received_user, userName, userAge, balance));

        // Раздел 6, задания 1-2: третий экран + передача boolean
        buttonAbout.setOnClickListener(v -> {
            Intent intent = new Intent(SecondActivity.this, AboutActivity.class);
            intent.putExtra(EXTRA_AGREED, checkBoxAgree.isChecked());
            startActivity(intent);
        });

        // Раздел 6, задание 5: неявный интент - откроется браузер
        buttonBrowser.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.browser_url)));
            try {
                startActivity(browserIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.error_no_browser, Toast.LENGTH_SHORT).show();
            }
        });

        buttonClose.setOnClickListener(v -> finish());
    }
}
