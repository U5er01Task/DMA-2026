package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 27. Электронный билет: ввод станций.
 */
public class TicketFormActivity extends AppCompatActivity {

    public static final String EXTRA_FROM = "EXTRA_FROM";
    public static final String EXTRA_TO = "EXTRA_TO";
    public static final String EXTRA_PASSENGER = "EXTRA_PASSENGER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_form);

        EditText editFrom = findViewById(R.id.editFrom);
        EditText editTo = findViewById(R.id.editTo);
        EditText editPassenger = findViewById(R.id.editPassenger);

        findViewById(R.id.buttonBuy).setOnClickListener(v -> {
            String from = editFrom.getText().toString().trim();
            String to = editTo.getText().toString().trim();
            String passenger = editPassenger.getText().toString().trim();
            if (from.isEmpty() || to.isEmpty() || passenger.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (from.equalsIgnoreCase(to)) {
                Toast.makeText(this, R.string.ticket_same_station, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, TicketActivity.class);
            intent.putExtra(EXTRA_FROM, from);
            intent.putExtra(EXTRA_TO, to);
            intent.putExtra(EXTRA_PASSENGER, passenger);
            startActivity(intent);
        });
    }
}
