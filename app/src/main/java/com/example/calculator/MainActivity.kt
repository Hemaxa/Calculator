package com.example.calculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.activity.viewModels // Импортируем делегат
import androidx.lifecycle.Observer // Импортируем Observer
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    // Ключи для SharedPreferences
    companion object {
        private const val PREFS_NAME = "CalculatorPrefs"
        private const val KEY_CURRENT_INPUT = "currentInput"
        private const val KEY_FIRST_OPERAND = "firstOperand"
        private const val KEY_PENDING_OPERATION = "pendingOperation"
        private const val KEY_WAITING_FOR_SECOND = "waitingForSecondOperand"
    }

    private lateinit var tvResult: TextView
    private lateinit var scrollView: HorizontalScrollView // Для прокрутки (Пункт 4)

    // Инициализируем ViewModel.
    // 'by viewModels()' автоматически создает и сохраняет ViewModel
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)
        scrollView = findViewById(R.id.tvResultScrollView) // (Пункт 4)

        // Загружаем сохраненное состояние, если оно есть
        if (!viewModel.stateRestored) {
            restoreState()
            viewModel.stateRestored = true
        }

        setupButtonClickListeners()

        // *** САМОЕ ВАЖНОЕ ***
        // Мы "подписываемся" на изменения в viewModel.currentInput.
        // Как только .value изменится, этот код выполнится
        // и обновит tvResult.text.
        viewModel.currentInput.observe(this, Observer { newText ->
            tvResult.text = newText
            // (Пункт 4) Прокручиваем в конец при обновлении
            scrollView.post { scrollView.fullScroll(View.FOCUS_RIGHT) }
        })
    }

    // Сохраняем состояние при остановке приложения
    override fun onStop() {
        super.onStop()
        saveState()
    }

    private fun saveState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putString(KEY_CURRENT_INPUT, viewModel.currentInput.value)
        // firstOperand может быть null, поэтому сохраняем его как String
        prefs.putString(KEY_FIRST_OPERAND, viewModel.firstOperand?.toString())
        prefs.putString(KEY_PENDING_OPERATION, viewModel.pendingOperation)
        prefs.putBoolean(KEY_WAITING_FOR_SECOND, viewModel.waitingForSecondOperand)
        prefs.apply()
    }

    private fun restoreState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        viewModel.currentInput.value = prefs.getString(KEY_CURRENT_INPUT, "0")

        val savedOperand = prefs.getString(KEY_FIRST_OPERAND, null)
        if (savedOperand != null) {
            viewModel.firstOperand = savedOperand.toDoubleOrNull()
        } else {
            viewModel.firstOperand = null
        }

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