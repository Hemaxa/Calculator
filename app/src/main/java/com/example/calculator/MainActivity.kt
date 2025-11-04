//класс, отвечающий за логику приложения

package com.example.calculator

import android.content.Context //библиотека для работы с SharedPreferences
import androidx.appcompat.app.AppCompatActivity //класс активности в Android
import android.os.Bundle //класс передачи данных между компонентами
import android.view.Menu //класс меню
import android.view.MenuItem //класс элементов меню
import android.view.View //класс пользовательских интерфейсов
import android.widget.Button //класс кнопки
import android.widget.HorizontalScrollView //класс горизонтальной прокрутки (для поля ввода)
import android.widget.TextView //текстовый ввод
import androidx.activity.viewModels //сохранение настроек в активности
import androidx.lifecycle.Observer //класс обновления элементов интерфейса
import java.text.DecimalFormat //форматирование чисел
import kotlin.math.pow //возведение в степень
import kotlin.math.sqrt //извлечение корня

class MainActivity : AppCompatActivity() {

    //все поля класса, необходимые для SharedPreferences
    companion object {
        private const val PREFS_NAME = "CalculatorPrefs"
        private const val KEY_CURRENT_INPUT = "currentInput"
        private const val KEY_FIRST_OPERAND = "firstOperand"
        private const val KEY_PENDING_OPERATION = "pendingOperation"
        private const val KEY_WAITING_FOR_SECOND = "waitingForSecondOperand"
        private const val KEY_CURRENT_THEME = "currentTheme"
    }

    //предварительное объявление переменных для UI
    private lateinit var tvResult: TextView
    private lateinit var scrollView: HorizontalScrollView

    //инициализация ViewModel, которая автоматически создает (или загружает) CalculatorViewModel
    private val viewModel: CalculatorViewModel by viewModels()

    //метод onCreate вызывается при создании экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        //применение сохраненной темы
        applySavedTheme()

        //сборка шаблона окна
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //связывание созданных элементов с их предварительными объявлениями
        tvResult = findViewById(R.id.tvResult)
        scrollView = findViewById(R.id.tvResultScrollView)

        //if отработает, если это первый запуск приложения (проверка на поворот экрана)
        if (!viewModel.stateRestored) {
            restoreState()
            viewModel.stateRestored = true
        }

        //логика кнопок
        setupButtonClickListeners()

        //сохранение ввода (currentInput) каждый раз, когда он меняется
        viewModel.currentInput.observe(this, Observer { newText ->
            tvResult.text = newText
            scrollView.post { scrollView.fullScroll(View.FOCUS_RIGHT) } //прокрутка максимально вправо
        })
    }

    //метод onStop вызывается при сворачивании приложения
    override fun onStop() {
        super.onStop()
        saveState()
    }

    //метод применения темы
    private fun applySavedTheme() {
        //выгрузка всех настроек в prefs
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        //установка темы (по умолчанию применяется голубая тема)
        val themeName = prefs.getString(KEY_CURRENT_THEME, "Blue")

        //обработка различных тем
        when (themeName) {
            "Orange" -> setTheme(R.style.Theme_Calculator_PastelOrange)
            "Pink" -> setTheme(R.style.Theme_Calculator_PastelPink)
            "Blue" -> setTheme(R.style.Theme_Calculator)
        }
    }

    //метод сборки меню с темами
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    //метод обработки нажатия на кнопки тем в меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()

        //when по всем темам
        when (item.itemId)
        {
            R.id.theme_blue -> {
                prefs.putString(KEY_CURRENT_THEME, "Blue")
            }
            R.id.theme_orange -> {
                prefs.putString(KEY_CURRENT_THEME, "Orange")
            }
            R.id.theme_pink -> {
                prefs.putString(KEY_CURRENT_THEME, "Pink")
            }
            else -> return super.onOptionsItemSelected(item)
        }

        prefs.apply() //сохранение выбора
        recreate() //пересоздание Activity, чтобы применить тему
        return true
    }

    //метод сохранения настроек в SharedPreferences (вызывается в onStop)
    private fun saveState() {
        //выгрузка текущих настроек в prefs
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()

        //запись настроек
        prefs.putString(KEY_CURRENT_INPUT, viewModel.currentInput.value)
        prefs.putString(KEY_FIRST_OPERAND, viewModel.firstOperand?.toString())
        prefs.putString(KEY_PENDING_OPERATION, viewModel.pendingOperation)
        prefs.putBoolean(KEY_WAITING_FOR_SECOND, viewModel.waitingForSecondOperand)

        //сохранение
        prefs.apply()
    }

    //метод выгрузки настроек из SharedPreferences (вызывается в onCreate)
    private fun restoreState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        //получение введенной строки
        viewModel.currentInput.value = prefs.getString(KEY_CURRENT_INPUT, "0")

        //проверка, что строка не пустая
        val savedOperand = prefs.getString(KEY_FIRST_OPERAND, null)

        //перевод из string в double
        if (savedOperand != null) {
            viewModel.firstOperand = savedOperand.toDoubleOrNull()
        }
        else {
            viewModel.firstOperand = null
        }

        //чтение операции и состояния ввода второго операнда
        viewModel.pendingOperation = prefs.getString(KEY_PENDING_OPERATION, null)
        viewModel.waitingForSecondOperand = prefs.getBoolean(KEY_WAITING_FOR_SECOND, false)
    }

    private fun setupButtonClickListeners() {
        val buttonIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnAC,
            R.id.btnSign, R.id.btnRoot, R.id.btnPower,
            R.id.btnDivide, R.id.btnMultiply, R.id.btnSubtract,
            R.id.btnPlus, R.id.btnEquals, R.id.btnDecimal
        )

        buttonIds.forEach { id ->
            findViewById<Button>(id)?.setOnClickListener { onButtonClick(it) }
        }
    }

    // Логика кнопок теперь читает и пишет в viewModel

    private fun onButtonClick(view: View) {
        val button = view as Button
        val buttonText = button.text.toString()

        when (buttonText) {
            in "0".."9" -> onDigitPressed(buttonText)
            "." -> onDecimalPointPressed()
            "AC" -> onClearPressed()
            "√" -> onRootPressed()
            "±" -> onSignPressed()
            "=" -> onEqualsPressed()
            else -> onOperatorPressed(buttonText)
        }
        // updateDisplay() БОЛЬШЕ НЕ НУЖЕН!
        // LiveData сделает это за нас.
    }

    private fun onDigitPressed(digit: String) {
        val currentText = viewModel.currentInput.value ?: "0"

        if (viewModel.waitingForSecondOperand) {
            viewModel.currentInput.value = digit
            viewModel.waitingForSecondOperand = false
        } else {
            if (currentText == "0" && digit != ".") {
                viewModel.currentInput.value = digit
            } else {
                viewModel.currentInput.value = currentText + digit
            }
        }
    }

    private fun onDecimalPointPressed() {
        if (viewModel.currentInput.value?.contains(".") == false) {
            viewModel.currentInput.value += "."
        }
    }

    private fun onOperatorPressed(operator: String) {
        val inputValue = viewModel.currentInput.value?.toDoubleOrNull() ?: return

        if (viewModel.firstOperand == null) {
            viewModel.firstOperand = inputValue
        } else if (viewModel.pendingOperation != null) {
            val result = performCalculation(viewModel.pendingOperation!!, viewModel.firstOperand!!, inputValue)
            val formattedResult = formatResult(result)
            viewModel.currentInput.value = formattedResult

            if (formattedResult == "Error") {
                viewModel.firstOperand = null
                viewModel.pendingOperation = null
                return
            }
            viewModel.firstOperand = result
        }
        viewModel.pendingOperation = operator
        viewModel.waitingForSecondOperand = true
    }

    private fun onEqualsPressed() {
        if (viewModel.firstOperand == null || viewModel.pendingOperation == null || viewModel.waitingForSecondOperand) {
            return
        }
        val inputValue = viewModel.currentInput.value?.toDoubleOrNull() ?: return
        val result = performCalculation(viewModel.pendingOperation!!, viewModel.firstOperand!!, inputValue)
        viewModel.currentInput.value = formatResult(result)
        viewModel.firstOperand = null
        viewModel.pendingOperation = null
        viewModel.waitingForSecondOperand = false
    }

    private fun performCalculation(operation: String, op1: Double, op2: Double): Double {
        return when (operation) {
            "+" -> op1 + op2
            "-" -> op1 - op2
            "×" -> op1 * op2
            "÷" -> if (op2 != 0.0) op1 / op2 else Double.NaN
            "xʸ" -> op1.pow(op2)
            else -> 0.0
        }
    }

    private fun onRootPressed() {
        val currentValue = viewModel.currentInput.value?.toDoubleOrNull()
        if (currentValue != null) {
            val result = if (currentValue >= 0) sqrt(currentValue) else Double.NaN
            viewModel.currentInput.value = formatResult(result)
            viewModel.waitingForSecondOperand = false
        }
    }

    private fun onClearPressed() {
        viewModel.currentInput.value = "0"
        viewModel.firstOperand = null
        viewModel.pendingOperation = null
        viewModel.waitingForSecondOperand = false
    }

    private fun onSignPressed() {
        if (viewModel.currentInput.value == "Error") return
        val currentValue = viewModel.currentInput.value?.toDoubleOrNull()
        if (currentValue != null && currentValue != 0.0) {
            viewModel.currentInput.value = formatResult(-currentValue)
        }
    }

    // updateDisplay() БОЛЬШЕ НЕ НУЖЕН!
    // private fun updateDisplay() { ... }

    private fun formatResult(result: Double): String {
        if (result.isNaN() || result.isInfinite()) {
            return "Error"
        }
        val formatter = DecimalFormat("0.########")
        return formatter.format(result)
    }
}