package ru.kulikov.pr4.level3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 26. Автокредит: аннуитетный платеж.
 * P = S * i / (1 - (1 + i)^-n), где i - месячная ставка, n - число месяцев.
 */
public class CarLoanActivity extends AppCompatActivity {

    public static final String EXTRA_AMOUNT = "EXTRA_AMOUNT";
    public static final String EXTRA_RATE = "EXTRA_RATE";
    public static final String EXTRA_MONTHS = "EXTRA_MONTHS";

    private double amount;
    private double yearRate;
    private int months;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_loan);

        EditText editAmount = findViewById(R.id.editAmount);
        EditText editRate = findViewById(R.id.editRate);
        EditText editMonths = findViewById(R.id.editMonths);
        TextView textPayment = findViewById(R.id.textPayment);
        Button buttonSchedule = findViewById(R.id.buttonSchedule);

        findViewById(R.id.buttonCalculate).setOnClickListener(v -> {
            Double a = InputUtils.readDouble(editAmount);
            Double r = InputUtils.readDouble(editRate);
            Integer n = InputUtils.readInt(editMonths);
            if (a == null || r == null || n == null || a <= 0 || r < 0 || n <= 0) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            amount = a;
            yearRate = r;
            months = n;

            double payment = monthlyPayment(amount, yearRate, months);
            double overpay = payment * months - amount;
            textPayment.setText(getString(R.string.loan_payment, payment, overpay));
            buttonSchedule.setEnabled(true);
        });

        buttonSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoanScheduleActivity.class);
            intent.putExtra(EXTRA_AMOUNT, amount);
            intent.putExtra(EXTRA_RATE, yearRate);
            intent.putExtra(EXTRA_MONTHS, months);
            startActivity(intent);
        });
    }

    public static double monthlyPayment(double amount, double yearRatePercent, int months) {
        double i = yearRatePercent / 12 / 100;
        if (i == 0) {
            return amount / months; // беспроцентная рассрочка
        }
        return amount * i / (1 - Math.pow(1 + i, -months));
    }
}
