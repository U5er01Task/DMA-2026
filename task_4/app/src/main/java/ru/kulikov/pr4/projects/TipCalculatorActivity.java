package ru.kulikov.pr4.projects;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Проект 2: Калькулятор чаевых (10%) и суммы с каждого гостя.
 */
public class TipCalculatorActivity extends AppCompatActivity {

    private static final double TIP_PERCENT = 0.10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);

        EditText etTotalBill = findViewById(R.id.etTotalBill);
        EditText etPersonsCount = findViewById(R.id.etPersonsCount);
        Button btnCalculate = findViewById(R.id.btnCalculate);
        TextView tvTipAmount = findViewById(R.id.tvTipAmount);
        TextView tvPerPerson = findViewById(R.id.tvPerPerson);

        tvTipAmount.setText(getString(R.string.tips_amount, 0.0));
        tvPerPerson.setText(getString(R.string.tips_per_person, 0.0));

        btnCalculate.setOnClickListener(v -> {
            String billStr = etTotalBill.getText().toString().trim();
            String personsStr = etPersonsCount.getText().toString().trim();

            if (billStr.isEmpty() || personsStr.isEmpty()) {
                Toast.makeText(this, R.string.tips_error_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // на русской клавиатуре может прийти запятая
                double bill = Double.parseDouble(billStr.replace(',', '.'));
                int persons = Integer.parseInt(personsStr);

                if (bill <= 0) {
                    Toast.makeText(this, R.string.tips_error_bill, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (persons <= 0) {
                    Toast.makeText(this, R.string.tips_error_persons, Toast.LENGTH_SHORT).show();
                    return;
                }

                double tip = bill * TIP_PERCENT;
                double perPerson = (bill + tip) / persons;

                tvTipAmount.setText(getString(R.string.tips_amount, tip));
                tvPerPerson.setText(getString(R.string.tips_per_person, perPerson));
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_number, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
