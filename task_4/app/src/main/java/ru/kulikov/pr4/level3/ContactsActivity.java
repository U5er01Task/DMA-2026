package ru.kulikov.pr4.level3;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 30. Портфолио: экран "Контакты" с переходом на GitHub.
 */
public class ContactsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_detail);

        ((TextView) findViewById(R.id.textTitle)).setText(R.string.portfolio_contacts);
        ((TextView) findViewById(R.id.textBody)).setText(R.string.contacts_text);

        Button buttonExtra = findViewById(R.id.buttonExtra);
        buttonExtra.setText(R.string.contacts_open_github);
        buttonExtra.setVisibility(View.VISIBLE);
        buttonExtra.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.contacts_github_url))));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.error_no_browser, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
