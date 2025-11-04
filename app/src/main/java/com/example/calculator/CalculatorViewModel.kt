// app/src/main/java/com/example/calculator/CalculatorViewModel.kt
package com.example.calculator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt

class CalculatorViewModel : ViewModel() {
    val currentInput = MutableLiveData<String>("0")
    var firstOperand: Double? = null
    var pendingOperation: String? = null
    var waitingForSecondOperand = false
    var stateRestored = false

    fun onDigitPressed(digit: String) {
        val currentText = currentInput.value ?: "0"
        if (waitingForSecondOperand) {
            currentInput.value = digit
            waitingForSecondOperand = false
        } else {
            if (currentText == "0" && digit != ".") {
                currentInput.value = digit
            } else {
                if (currentText.length < 15) {
                    currentInput.value = currentText + digit
                }
            }
        }
    }

    fun onDecimalPointPressed() {
        if (waitingForSecondOperand) {
            currentInput.value = "0."
            waitingForSecondOperand = false
            return
        }
        if (currentInput.value?.contains(".") == false) {
            currentInput.value += "."
        }
    }

    fun onOperatorPressed(operator: String) {
        val inputValue = currentInput.value?.toDoubleOrNull()
        if (inputValue == null && currentInput.value == "Error") {
            onClearPressed()
            return
        } else if (inputValue == null) {
            return
        }

        if (firstOperand == null) {
            firstOperand = inputValue
        } else if (pendingOperation != null) {
            if (waitingForSecondOperand) {
                pendingOperation = operator
                return
            }
            val result = performCalculation(pendingOperation!!, firstOperand!!, inputValue)
            val formattedResult = formatResult(result)
            currentInput.value = formattedResult
            if (formattedResult == "Error") {
                onClearPressed()
                return
            }
            firstOperand = result
        }
        pendingOperation = operator
        waitingForSecondOperand = true
    }

    fun onEqualsPressed() {
        if (firstOperand == null || pendingOperation == null || waitingForSecondOperand) {
            return
        }
        val inputValue = currentInput.value?.toDoubleOrNull() ?: return
        val result = performCalculation(pendingOperation!!, firstOperand!!, inputValue)
        currentInput.value = formatResult(result)
        firstOperand = null
        pendingOperation = null
        waitingForSecondOperand = false
    }

    private fun performCalculation(operation: String, op1: Double, op2: Double): Double {
        return try {
            when (operation) {
                "+" -> op1 + op2
                "-" -> op1 - op2
                "×" -> op1 * op2
                "÷" -> if (op2 != 0.0) op1 / op2 else Double.NaN
                "xʸ" -> op1.pow(op2)
                else -> 0.0
            }
        } catch (e: Exception) {
            Double.NaN
        }
    }

    fun onRootPressed() {
        if (waitingForSecondOperand) return
        val currentValue = currentInput.value?.toDoubleOrNull()
        if (currentValue != null) {
            val result = if (currentValue >= 0) sqrt(currentValue) else Double.NaN
            currentInput.value = formatResult(result)
            waitingForSecondOperand = false
            firstOperand = null
            pendingOperation = null
        }
    }

    fun onClearPressed() {
        currentInput.value = "0"
        firstOperand = null
        pendingOperation = null
        waitingForSecondOperand = false
    }

    fun onSignPressed() {
        if (waitingForSecondOperand) return
        if (currentInput.value == "Error") return
        val currentValue = currentInput.value?.toDoubleOrNull()
        if (currentValue != null && currentValue != 0.0) {
            currentInput.value = formatResult(-currentValue)
        }
    }

    private fun formatResult(result: Double): String {
        if (result.isNaN() || result.isInfinite()) {
            return "Error"
        }
        val formatter = DecimalFormat("0.########")
        var formatted = formatter.format(result)
        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length - 2)
        }
        if (formatted.length > 15 && formatted.contains("E")) {
            // OK
        } else if (formatted.length > 15) {
            return "Error"
        }
        return formatted
    }
}