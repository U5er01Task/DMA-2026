package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 24. Конструктор визитки мастера: ввод данных.
 */
public class MasterFormActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_PROFESSION = "EXTRA_PROFESSION";
    public static final String EXTRA_PHONE = "EXTRA_PHONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_form);

        EditText editName = findViewById(R.id.editName);
        EditText editProfession = findViewById(R.id.editProfession);
        EditText editPhone = findViewById(R.id.editPhone);

        findViewById(R.id.buttonCreate).setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String profession = editProfession.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            if (name.isEmpty() || profession.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, MasterCardActivity.class);
            intent.putExtra(EXTRA_NAME, name);
            intent.putExtra(EXTRA_PROFESSION, profession);
            intent.putExtra(EXTRA_PHONE, phone);
            startActivity(intent);
        });
    }
}
