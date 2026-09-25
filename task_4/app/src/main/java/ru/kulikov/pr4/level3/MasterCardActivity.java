package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 24. Стилизованная визитка с переходом в звонилку (ACTION_DIAL).
 */
public class MasterCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_card);

        String phone = getIntent().getStringExtra(MasterFormActivity.EXTRA_PHONE);
        ((TextView) findViewById(R.id.textName)).setText(getIntent().getStringExtra(MasterFormActivity.EXTRA_NAME));
        ((TextView) findViewById(R.id.textProfession)).setText(getIntent().getStringExtra(MasterFormActivity.EXTRA_PROFESSION));
        ((TextView) findViewById(R.id.textPhone)).setText(phone);

        // ACTION_DIAL только открывает звонилку с номером - разрешение CALL_PHONE не нужно
        findViewById(R.id.buttonCall).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))));
    }
}
