package ru.kulikov.pr4;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import ru.kulikov.pr4.basics.GreetingActivity;
import ru.kulikov.pr4.level1.CharCounterActivity;
import ru.kulikov.pr4.level1.CmToInchActivity;
import ru.kulikov.pr4.level1.DiceActivity;
import ru.kulikov.pr4.level1.FlashlightActivity;
import ru.kulikov.pr4.level1.ParityActivity;
import ru.kulikov.pr4.level1.PetAgeActivity;
import ru.kulikov.pr4.level1.QuizActivity;
import ru.kulikov.pr4.level1.ReverseActivity;
import ru.kulikov.pr4.level1.ToggleActivity;
import ru.kulikov.pr4.level1.TrafficLightActivity;
import ru.kulikov.pr4.level2.BmiActivity;
import ru.kulikov.pr4.level2.CalculatorActivity;
import ru.kulikov.pr4.level2.CapitalsQuizActivity;
import ru.kulikov.pr4.level2.CurrencyActivity;
import ru.kulikov.pr4.level2.DiscountActivity;
import ru.kulikov.pr4.level2.FuelActivity;
import ru.kulikov.pr4.level2.PasswordStrengthActivity;
import ru.kulikov.pr4.level2.PinGeneratorActivity;
import ru.kulikov.pr4.level2.TemperatureActivity;
import ru.kulikov.pr4.level2.TravelTimeActivity;
import ru.kulikov.pr4.level3.CarLoanActivity;
import ru.kulikov.pr4.level3.GlossaryActivity;
import ru.kulikov.pr4.level3.LoginActivity;
import ru.kulikov.pr4.level3.MasterFormActivity;
import ru.kulikov.pr4.level3.NoteEditActivity;
import ru.kulikov.pr4.level3.PizzaOrderActivity;
import ru.kulikov.pr4.level3.PortfolioActivity;
import ru.kulikov.pr4.level3.QuizQuestion1Activity;
import ru.kulikov.pr4.level3.StepsFormActivity;
import ru.kulikov.pr4.level3.TicketFormActivity;
import ru.kulikov.pr4.projects.ClickerActivity;
import ru.kulikov.pr4.projects.StudentFormActivity;
import ru.kulikov.pr4.projects.TipCalculatorActivity;

/**
 * Главное меню: все задания практической работы №4 в одном приложении.
 */
public class MainActivity extends AppCompatActivity {

    private LinearLayout menuContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menuContainer = findViewById(R.id.menuContainer);

        addSection(R.string.section_basics);
        addTask(R.string.basics_greeting, GreetingActivity.class);

        addSection(R.string.section_projects);
        addTask(R.string.project_clicker, ClickerActivity.class);
        addTask(R.string.project_tips, TipCalculatorActivity.class);
        addTask(R.string.project_card, StudentFormActivity.class);

        addSection(R.string.section_level1);
        addTask(R.string.l1_traffic, TrafficLightActivity.class);
        addTask(R.string.l1_dice, DiceActivity.class);
        addTask(R.string.l1_quiz, QuizActivity.class);
        addTask(R.string.l1_toggle, ToggleActivity.class);
        addTask(R.string.l1_reverse, ReverseActivity.class);
        addTask(R.string.l1_pet, PetAgeActivity.class);
        addTask(R.string.l1_chars, CharCounterActivity.class);
        addTask(R.string.l1_flashlight, FlashlightActivity.class);
        addTask(R.string.l1_inches, CmToInchActivity.class);
        addTask(R.string.l1_parity, ParityActivity.class);

        addSection(R.string.section_level2);
        addTask(R.string.l2_bmi, BmiActivity.class);
        addTask(R.string.l2_fuel, FuelActivity.class);
        addTask(R.string.l2_temperature, TemperatureActivity.class);
        addTask(R.string.l2_calculator, CalculatorActivity.class);
        addTask(R.string.l2_discount, DiscountActivity.class);
        addTask(R.string.l2_currency, CurrencyActivity.class);
        addTask(R.string.l2_pin, PinGeneratorActivity.class);
        addTask(R.string.l2_capitals, CapitalsQuizActivity.class);
        addTask(R.string.l2_travel, TravelTimeActivity.class);
        addTask(R.string.l2_password, PasswordStrengthActivity.class);

        addSection(R.string.section_level3);
        addTask(R.string.l3_login, LoginActivity.class);
        addTask(R.string.l3_pizza, PizzaOrderActivity.class);
        addTask(R.string.l3_quiz, QuizQuestion1Activity.class);
        addTask(R.string.l3_master, MasterFormActivity.class);
        addTask(R.string.l3_notes, NoteEditActivity.class);
        addTask(R.string.l3_loan, CarLoanActivity.class);
        addTask(R.string.l3_ticket, TicketFormActivity.class);
        addTask(R.string.l3_steps, StepsFormActivity.class);
        addTask(R.string.l3_glossary, GlossaryActivity.class);
        addTask(R.string.l3_portfolio, PortfolioActivity.class);
    }

    private void addSection(int titleRes) {
        TextView header = new TextView(this);
        header.setText(titleRes);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        header.setTextColor(getColor(R.color.brand_blue));
        header.setPadding(0, dp(20), 0, dp(4));
        menuContainer.addView(header);
    }

    private void addTask(int titleRes, Class<?> screen) {
        Button button = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(titleRes);
        button.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> startActivity(new Intent(this, screen)));
        menuContainer.addView(button);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
