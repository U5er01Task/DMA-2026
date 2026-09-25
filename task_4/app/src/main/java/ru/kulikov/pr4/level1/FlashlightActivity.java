package ru.kulikov.pr4.level1;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import ru.kulikov.pr4.R;

/**
 * Задание 8. Экранный фонарик: белый экран + максимальная яркость только для этого окна.
 */
public class FlashlightActivity extends AppCompatActivity {

    private boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashlight);

        View rootLayout = findViewById(R.id.rootLayout);
        Button button = findViewById(R.id.buttonFlashlight);

        button.setOnClickListener(v -> {
            isOn = !isOn;
            rootLayout.setBackgroundColor(getColor(isOn ? R.color.white : R.color.black));
            button.setText(isOn ? R.string.flashlight_off : R.string.flashlight_on);

            // яркость меняется только пока открыт этот экран, системная настройка не трогается
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = isOn
                    ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                    : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            getWindow().setAttributes(params);
        });
    }
}
