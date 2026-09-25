package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

import ru.kulikov.pr4.R;

/**
 * Задание 27. Посадочный купон с текущей датой.
 */
public class TicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        Intent intent = getIntent();
        String from = intent.getStringExtra(TicketFormActivity.EXTRA_FROM);
        String to = intent.getStringExtra(TicketFormActivity.EXTRA_TO);
        String passenger = intent.getStringExtra(TicketFormActivity.EXTRA_PASSENGER);

        Random random = new Random();
        String train = String.format(Locale.getDefault(), "%03dА", random.nextInt(900) + 100);
        int wagon = random.nextInt(12) + 1;
        int seat = random.nextInt(54) + 1;
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        ((TextView) findViewById(R.id.textRoute)).setText(getString(R.string.ticket_route, from, to));
        ((TextView) findViewById(R.id.textDetails)).setText(
                getString(R.string.ticket_details, passenger, date, train, wagon, seat));
        ((TextView) findViewById(R.id.textCode)).setText(
                String.format(Locale.getDefault(), "№ %010d", Math.abs(random.nextLong() % 10_000_000_000L)));
    }
}
