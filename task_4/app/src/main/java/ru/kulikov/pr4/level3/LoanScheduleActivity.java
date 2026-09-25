package ru.kulikov.pr4.level3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import ru.kulikov.pr4.R;

/**
 * Задание 26. График выплат по месяцам.
 */
public class LoanScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_schedule);

        double amount = getIntent().getDoubleExtra(CarLoanActivity.EXTRA_AMOUNT, 0);
        double yearRate = getIntent().getDoubleExtra(CarLoanActivity.EXTRA_RATE, 0);
        int months = getIntent().getIntExtra(CarLoanActivity.EXTRA_MONTHS, 1);

        double payment = CarLoanActivity.monthlyPayment(amount, yearRate, months);
        double monthRate = yearRate / 12 / 100;

        ((TextView) findViewById(R.id.textSummary)).setText(
                getString(R.string.loan_payment, payment, payment * months - amount));

        StringBuilder table = new StringBuilder();
        double balance = amount;
        for (int month = 1; month <= months; month++) {
            double interest = balance * monthRate;
            double principal = payment - interest;
            if (month == months) {
                principal = balance; // последний платеж закрывает остаток копейка в копейку
            }
            balance -= principal;
            table.append(String.format(Locale.getDefault(), "%3d %10.2f %10.2f %10.2f %10.2f%n",
                    month, principal + interest, interest, principal, Math.abs(balance)));
        }
        ((TextView) findViewById(R.id.textSchedule)).setText(table.toString());
    }
}
