# Практическая работа №4 — Первые Android-приложения на Java

**Дисциплина:** Разработка мобильных приложений
**Студент:** Куликов Евгений Анатольевич
**Группа:** П24-3.2

Задание: [task_4.md](https://github.com/U5er01Task/DMA-2026/blob/main/task_4.md)

## Что сделано

Все задания собраны в **одно приложение** «Первые приложения» с главным меню — каждое задание открывается отдельным экраном (Activity). Так проще показывать работу: один APK, а не 30 отдельных проектов.

- Разделы 3–6 — все задания для закрепления (разметка, Java, ресурсы, Intent)
- 3 учебных проекта из методички (кликер, калькулятор чаевых, визитка студента)
- 30 практических заданий (уровни 1–3)

Всего 48 экранов.

## Окружение

- Windows 10, JDK 21 (Eclipse Temurin)
- Android SDK: platform 35, build-tools 34/35
- Gradle 8.11.1 (wrapper), Android Gradle Plugin 8.7.3, Kotlin DSL
- Проверка на эмуляторе Pixel 7, Android 15 (API 35)
- `minSdk 26` (Android 8.0) — как советует методичка

## Как собрать и запустить

Открыть папку в Android Studio (File → Open) и нажать Run.

Или из командной строки:

```bat
gradlew.bat assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

## Структура

```
app/src/main/
├── AndroidManifest.xml              все 49 Activity (главное меню + 48 экранов)
├── java/ru/kulikov/pr4/
│   ├── MainActivity.java            главное меню
│   ├── InputUtils.java              чтение чисел из полей без вылета на пустой строке
│   ├── basics/                      разделы 3–6
│   ├── projects/                    3 проекта из методички
│   ├── level1/                      задания 1–10
│   ├── level2/                      задания 11–20
│   └── level3/                      задания 21–30 (двухэкранные)
└── res/
    ├── layout/                      разметка экранов (LinearLayout)
    ├── values/                      строки (по файлу на раздел), цвета, тема, plurals
    ├── values-en/                   английский перевод
    ├── drawable/                    векторные иконки
    └── mipmap-anydpi/               адаптивная иконка приложения
```

## Разделы 3–6: задания для закрепления

Все сделаны на экранах «Приветствие» → «Второй экран» → «Об авторе» (`basics/`).

| Раздел | Задание | Где |
| :-- | :-- | :-- |
| 3.1 | orientation horizontal | При horizontal у корня все элементы встают в одну строку и уезжают за экран. Корень оставил vertical, горизонтальный LinearLayout использовал для ряда из двух кнопок |
| 3.2 | Поле возраста `inputType="number"` | `activity_greeting.xml` |
| 3.3 | Синий заголовок `#1976D2` | цвет `brand_blue` |
| 3.4 | Отступ `layout_marginTop="20dp"` | поле возраста и кнопка |
| 3.5 | Кнопка «Очистить всё» красным | `buttonClear` |
| 4.1 | Toast при успешном приветствии | `GreetingActivity.greet()` |
| 4.2 | «Слишком короткое имя» (меньше 2 символов) | ошибка у поля ввода |
| 4.3 | Кнопка меняет заголовок | «Изменить заголовок» |
| 4.4 | Скрыть результат `View.GONE` | «Скрыть ответ» / «Показать ответ» |
| 4.5 | Счетчик нажатий | «Счетчик +1» |
| 5.1 | Все строки в `strings.xml` | в layout нет захардкоженного текста |
| 5.2 | Цвета `brand_blue`, `brand_green`, `brand_gray` | `colors.xml`, фон и кнопки |
| 5.3 | Векторная иконка в ImageView | `drawable/ic_student.xml` (Material «school») |
| 5.4 | `welcome_user` с `%1$s` | `getString(R.string.welcome_user, name)` |
| 5.5 | Английская локализация | `values-en/` — переведено всё приложение |
| 6.1 | Третий экран «Об авторе» с кнопкой выхода | `AboutActivity` |
| 6.2 | Передача `boolean` со второго экрана на третий | чекбокс «Я согласен с правилами» |
| 6.3 | `null` → «Гость» | если имя не ввели |
| 6.4 | Передача `double` | баланс 1520.75 |
| 6.5 | Открытие браузера неявным Intent | «Открыть браузер» |

## Скриншоты

### Сборка (Windows 10)

![Сборка](screenshots/build-windows.png)

### Главное меню и разделы 3–6

| Меню | Приветствие | Второй экран | Об авторе |
| :-: | :-: | :-: | :-: |
| ![](screenshots/00-menu.png) | ![](screenshots/01-greeting.png) | ![](screenshots/02-second-screen.png) | ![](screenshots/03-about.png) |

### Учебные проекты

| Тап-кликер | Калькулятор чаевых | Визитка студента |
| :-: | :-: | :-: |
| ![](screenshots/04-clicker.png) | ![](screenshots/05-tips.png) | ![](screenshots/06-student-card.png) |

### Уровень 1

| 1. Светофор | 2. Кубик | 3. Мини-тест | 4. Секрет | 5. Инвертор |
| :-: | :-: | :-: | :-: | :-: |
| ![](screenshots/07-traffic-light.png) | ![](screenshots/08-dice.png) | ![](screenshots/09-quiz.png) | ![](screenshots/10-toggle.png) | ![](screenshots/11-reverse.png) |

| 6. Питомец | 7. Символы | 8. Фонарик | 9. Дюймы | 10. Четность |
| :-: | :-: | :-: | :-: | :-: |
| ![](screenshots/12-pet-age.png) | ![](screenshots/13-char-counter.png) | ![](screenshots/14-flashlight.png) | ![](screenshots/15-cm-to-inch.png) | ![](screenshots/16-parity.png) |

### Уровень 2

| 11. ИМТ | 12. Топливо | 13. Температура | 14. Деление на 0 | 14. Калькулятор |
| :-: | :-: | :-: | :-: | :-: |
| ![](screenshots/17-bmi.png) | ![](screenshots/18-fuel.png) | ![](screenshots/19-temperature.png) | ![](screenshots/20a-calculator-div-zero.png) | ![](screenshots/20b-calculator.png) |

| 15. Скидка | 16. Валюты | 17. PIN | 18. Столицы | 19. Время в пути | 20. Пароль |
| :-: | :-: | :-: | :-: | :-: | :-: |
| ![](screenshots/21-discount.png) | ![](screenshots/22-currency.png) | ![](screenshots/23-pin.png) | ![](screenshots/24-capitals.png) | ![](screenshots/25-travel-time.png) | ![](screenshots/26-password.png) |

### Уровень 3

| 21. Неверный пароль | 21. Кабинет | 22. Чек пиццы | 23. Вопрос 1 | 23. Результат |
| :-: | :-: | :-: | :-: | :-: |
| ![](screenshots/27a-login-error.png) | ![](screenshots/27b-cabinet.png) | ![](screenshots/28-pizza-receipt.png) | ![](screenshots/29a-quiz-q1.png) | ![](screenshots/29b-quiz-result.png) |

| 24. Визитка | 24. Звонилка | 25. Заметка | 26. Кредит | 26. График |
| :-: | :-: | :-: | :-: | :-: |
| ![](screenshots/30a-master-card.png) | ![](screenshots/30b-dialer.png) | ![](screenshots/31-note-read.png) | ![](screenshots/32a-loan.png) | ![](screenshots/32b-loan-schedule.png) |

| 27. Билет | 28. Шаги | 29. Термин | 30. Портфолио | 30. Навыки |
| :-: | :-: | :-: | :-: | :-: |
| ![](screenshots/33-ticket.png) | ![](screenshots/34-steps.png) | ![](screenshots/35-glossary-term.png) | ![](screenshots/36a-portfolio.png) | ![](screenshots/36b-portfolio-skills.png) |

### Английская локализация (раздел 5, задание 5)

| Меню | Приветствие |
| :-: | :-: |
| ![](screenshots/37a-english-menu.png) | ![](screenshots/37b-english-greeting.png) |

Имена и адреса в примерах набраны латиницей — эмулятор вводил текст через `adb`, а он не умеет кириллицу.

## Ошибки, найденные в методичке

- В проектах 1 и 3 в разметке стоит `android:backgroundColor` — такого атрибута у View нет, сборка падает с ошибкой `attribute android:backgroundColor not found`. Исправил на `android:background`.
- В кликере при повороте экрана счет сбрасывался (ошибка №8 из таблицы). Сохраняю счет в `onSaveInstanceState`.
- В калькуляторе чаевых на русской клавиатуре десятичный разделитель — запятая, и `Double.parseDouble("12,5")` падает. Заменяю запятую на точку перед разбором.

## Проверка

- `gradlew assembleDebug` — сборка без ошибок
- `gradlew lintDebug` — 0 ошибок. Lint помог найти две вещи: неполный английский перевод (сначала были переведены только разделы 3–6) и строки, где нужны plurals («1 год / 2 года / 5 лет», «остался 1 шаг / осталось 5 шагов»)
- Все 48 экранов пройдены на эмуляторе, скриншоты выше

## Вывод

Научился создавать Android-приложения на Java: верстать экраны в XML (LinearLayout, EditText, Button, RadioGroup, CheckBox, ProgressBar), связывать разметку с кодом через `findViewById`, обрабатывать нажатия, выносить строки и цвета в ресурсы, делать перевод на другой язык и передавать данные между экранами через Intent (строки, числа, boolean, double). Также разобрался с неявными интентами (браузер, звонилка) и с тем, почему приложение не должно падать на пустом поле ввода.
