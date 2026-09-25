package ru.kulikov.pr4;

import android.widget.EditText;

/**
 * Чтение чисел из полей ввода без падения приложения на пустой строке.
 */
public final class InputUtils {

    private InputUtils() {
    }

    /** @return число или null, если поле пустое или там не число */
    public static Double readDouble(EditText field) {
        String text = field.getText().toString().trim().replace(',', '.');
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** @return целое число или null */
    public static Integer readInt(EditText field) {
        String text = field.getText().toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
