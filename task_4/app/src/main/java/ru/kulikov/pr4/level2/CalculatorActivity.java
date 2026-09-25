package ru.kulikov.pr4.level2;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ru.kulikov.pr4.InputUtils;
import ru.kulikov.pr4.R;

/**
 * Задание 14. Калькулятор на 4 действия с защитой от деления на ноль.
 */
public class CalculatorActivity extends AppCompatActivity {

    private EditText editFirst;
    private EditText editSecond;
    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        editFirst = findViewById(R.id.editFirst);
        editSecond = findViewById(R.id.editSecond);
        textResult = findViewById(R.id.textResult);

        findViewById(R.id.buttonPlus).setOnClickListener(v -> calculate('+'));
        findViewById(R.id.buttonMinus).setOnClickListener(v -> calculate('-'));
        findViewById(R.id.buttonMultiply).setOnClickListener(v -> calculate('*'));
        findViewById(R.id.buttonDivide).setOnClickListener(v -> calculate('/'));
    }

    private void calculate(char operation) {
        Double a = InputUtils.readDouble(editFirst);
        Double b = InputUtils.readDouble(editSecond);
        if (a == null || b == null) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double result;
        switch (operation) {
            case '+':
                result = a + b;
                break;
            case '-':
                result = a - b;
                break;
            case '*':
                result = a * b;
                break;
            default:
                if (b == 0) {
                    textResult.setText(R.string.calc_div_zero);
                    textResult.setTextColor(getColor(R.color.error_red));
                    return;
                }
                result = a / b;
                break;
        }
        textResult.setTextColor(getColor(R.color.text_primary));
        textResult.setText(getString(R.string.calc_result, format(result)));
    }

    /** 5.0 -> "5", 0.1 + 0.2 -> "0.3" (округляем до 10 знаков и убираем лишние нули) */
    private String format(double value) {
        BigDecimal number = BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        return number.toPlainString();
    }
}
