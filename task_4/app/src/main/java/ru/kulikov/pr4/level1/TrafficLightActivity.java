package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 1. Светофор: кнопка меняет цвет фона всего экрана.
 */
public class TrafficLightActivity extends AppCompatActivity {

    private View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_light);

        rootLayout = findViewById(R.id.rootLayout);
        findViewById(R.id.buttonRed).setOnClickListener(v -> setLight(R.color.light_red));
        findViewById(R.id.buttonYellow).setOnClickListener(v -> setLight(R.color.light_yellow));
        findViewById(R.id.buttonGreen).setOnClickListener(v -> setLight(R.color.light_green));
    }

    private void setLight(int colorRes) {
        rootLayout.setBackgroundColor(getColor(colorRes));
    }
}
