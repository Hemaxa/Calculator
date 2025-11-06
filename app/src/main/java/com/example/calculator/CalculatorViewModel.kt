//класс, отвечающий за сами вычисления

package com.example.calculator

import androidx.lifecycle.MutableLiveData //необходимо для хранения наблюдаемого состояния
import androidx.lifecycle.ViewModel //управление состоянием данных
import java.text.DecimalFormat //удобное форматирование чисел
import kotlin.math.pow //возведение в степень
import kotlin.math.sqrt //извлечение корня

class CalculatorViewModel : ViewModel() {

    //состояния (память калькулятора)
    val currentInput = MutableLiveData<String>("0")
    var firstOperand: Double? = null
    var pendingOperation: String? = null
    var waitingForSecondOperand = false
    var stateRestored = false

    //переменные для повторного действия
    private var lastOperation: String? = null
    private var lastSecondOperand: Double? = null

    //метод нажатия кнопки с цифрой
    fun onDigitPressed(digit: String) {
        //сбрасывается состояние повтора, т.к. начинается новый ввод
        if (firstOperand == null && pendingOperation == null) {
            resetLastOperation()
        }

        val currentText = currentInput.value ?: "0"

        //ожидние ввода второго числа
        if (waitingForSecondOperand) {
            currentInput.value = digit //сохранение первого числа
            waitingForSecondOperand = false //сброс флага
        }
        //если первое
        else {
            //ноль меняется на цифру
            if (currentText == "0" && digit != ".") {
                currentInput.value = digit
            }
            //если уже есть цифры, то новые добавляются в конец
            else {
                if (currentText.length < 15) {
                    currentInput.value = currentText + digit
                }
            }
        }
    }

    //метод нажатия на "."
    fun onDecimalPointPressed() {
        //ввод нецелого второго числа
        if (waitingForSecondOperand) {
            currentInput.value = "0."
            waitingForSecondOperand = false
            return
        }
        //обработка повторного нажатия точки
        if (currentInput.value?.contains(".") == false) {
            currentInput.value += "."
        }
    }

    //метод нажатия на оператор
    fun onOperatorPressed(operator: String) {
        //новая операция всегда сбрасывает повтор равенства
        resetLastOperation()

        //перевод ввода в число
        val inputValue = currentInput.value?.toDoubleOrNull()

        //если на экране Error, то все сбрасывается
        if (inputValue == null && currentInput.value == "Error") {
            onClearPressed()
            return
        }
        else if (inputValue == null) {
            return
        }

        //если нет первого операнда, то сохранение текущего о в firstOperand
        if (firstOperand == null) {
            firstOperand = inputValue
        }
        //если он уже есть, то значит это цепочка операций
        else if (pendingOperation != null) {
            //если мы ожидали число, а нажата операция, то меняется оператор
            if (waitingForSecondOperand) {
                pendingOperation = operator
                return
            }

            //выполняется вычисление
            val result = performCalculation(pendingOperation!!, firstOperand!!, inputValue)

            //форматирование результата
            val formattedResult = formatResult(result)

            //вывод результата
            currentInput.value = formattedResult

            //если недопустимая операция, то вывод Error
            if (formattedResult == "Error") {
                onClearPressed()
                return
            }
            //результат вычисления сохраняется как новый первый операнд
            firstOperand = result
        }

        //запоминание оператора и переход в режим ожидания второго числа
        pendingOperation = operator
        waitingForSecondOperand = true
    }

    //метод нажатия на "="
    fun onEqualsPressed() {
        //чтение введенной строки
        val inputValue = currentInput.value?.toDoubleOrNull()

        //если на экране "Error" или нет числа, ничего не происходит
        if (inputValue == null) {
            if (currentInput.value == "Error") onClearPressed()
            return
        }

        //повторное нажатие "="
        //firstOperand сброшен до null, lastOperation не null
        if (firstOperand == null && pendingOperation == null && lastOperation != null && lastSecondOperand != null) {
            val result = performCalculation(lastOperation!!, inputValue, lastSecondOperand!!)
            currentInput.value = formatResult(result)
        }
        //обычное нажатие "="
        else if (firstOperand != null && pendingOperation != null && !waitingForSecondOperand) {
            //выполняется вычисление
            val result = performCalculation(pendingOperation!!, firstOperand!!, inputValue)
            currentInput.value = formatResult(result)

            //сохраняется операция и второй операнд для повторного нажатия
            lastOperation = pendingOperation
            lastSecondOperand = inputValue

            //сброс состояний
            firstOperand = null
            pendingOperation = null
            waitingForSecondOperand = false
        }
    }

    //метод выполнения вычислений
    private fun performCalculation(operation: String, op1: Double, op2: Double): Double {
        //отлов ошибок
        return try {
            //when по операциям
            when (operation) {
                "+" -> op1 + op2
                "-" -> op1 - op2
                "×" -> op1 * op2
                "÷" -> if (op2 != 0.0) op1 / op2 else Double.NaN
                "xʸ" -> op1.pow(op2)
                else -> 0.0
            }
        }
        //обработка ошибки - перевод в NaN
        catch (e: Exception) {
            Double.NaN
        }
    }

    //метод нажатия на корень
    fun onRootPressed() {
        //сброс предыдущей операции
        resetLastOperation()

        //выход, если ожидается второе число
        if (waitingForSecondOperand) return

        //получение числа с экрана
        val currentValue = currentInput.value?.toDoubleOrNull()
        if (currentValue != null) {
            val result = if (currentValue >= 0) sqrt(currentValue) else Double.NaN
            currentInput.value = formatResult(result)
            waitingForSecondOperand = false
            firstOperand = null
            pendingOperation = null
        }
    }

    //метод очистки поля ввода
    fun onClearPressed() {
        //сбрасывание значений всех полей
        currentInput.value = "0"
        firstOperand = null
        pendingOperation = null
        waitingForSecondOperand = false

        //сброс предыдущей операции
        resetLastOperation()
    }

    //метод смены знака
    fun onSignPressed() {
        //сброс предыдущей операции
        resetLastOperation()

        //выход, если ожидается второе число
        if (waitingForSecondOperand) return
        if (currentInput.value == "Error") return
        val currentValue = currentInput.value?.toDoubleOrNull()
        if (currentValue != null && currentValue != 0.0) {
            currentInput.value = formatResult(-currentValue)
        }
    }

    //метод сброса последней операции
    private fun resetLastOperation() {
        lastOperation = null
        lastSecondOperand = null
    }

    //метод обработки красивого вывода числа
    private fun formatResult(result: Double): String {
        //обработка Error
        if (result.isNaN() || result.isInfinite()) {
            return "Error"
        }

        //форматирование с 8 знаками после запятой
        val formatter = DecimalFormat("0.########")
        var formatted = formatter.format(result)

        //убирание .0 у целого числа
        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length - 2)
        }

        //если строка слишком длинная, то она принудительно форматируется в научную нотацию
        if (formatted.length > 15 && !formatted.contains("E")) {
            val scientificFormatter = DecimalFormat("0.#######E0")
            formatted = scientificFormatter.format(result)
        }
        //если и научная нотация вышла за предел, то Error
        else if (formatted.length > 15) {
            val scientificFormatter = DecimalFormat("0.#######E0")
            formatted = scientificFormatter.format(result)
            if (formatted.length > 15) return "Error"
        }
        return formatted
    }
}