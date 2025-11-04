// app/src/main/java/com/example/calculator/MainActivity.kt
package com.example.calculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.calculator.ui.theme.CalculatorAppTheme
import com.example.calculator.ui.theme.CalculatorThemeName

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "CalculatorPrefs"
        private const val KEY_CURRENT_INPUT = "currentInput"
        private const val KEY_FIRST_OPERAND = "firstOperand"
        private const val KEY_PENDING_OPERATION = "pendingOperation"
        private const val KEY_WAITING_FOR_SECOND = "waitingForSecondOperand"
        private const val KEY_CURRENT_THEME = "currentTheme"
    }

    private val viewModel: CalculatorViewModel by viewModels()
    private var currentTheme by mutableStateOf(CalculatorThemeName.Blue)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Удаляем enableEdgeToEdge(), он не нужен для калькулятора
        // enableEdgeToEdge()

        currentTheme = loadSavedTheme()

        if (!viewModel.stateRestored) {
            restoreCalculatorState()
            viewModel.stateRestored = true
        }

        setContent {
            // Используем нашу новую CalculatorAppTheme
            CalculatorAppTheme(themeName = currentTheme) {
                // Вызываем наш CalculatorScreen
                CalculatorScreen(
                    viewModel = viewModel,
                    currentThemeName = currentTheme,
                    onThemeChange = { newTheme ->
                        currentTheme = newTheme
                        saveTheme(newTheme)
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        saveCalculatorState()
    }

    private fun loadSavedTheme(): CalculatorThemeName {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeName = prefs.getString(KEY_CURRENT_THEME, "Blue")
        return when (themeName) {
            "Orange" -> CalculatorThemeName.Orange
            "Pink" -> CalculatorThemeName.Pink
            else -> CalculatorThemeName.Blue
        }
    }

    private fun saveTheme(themeName: CalculatorThemeName) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putString(KEY_CURRENT_THEME, themeName.name)
        prefs.apply()
    }

    private fun saveCalculatorState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putString(KEY_CURRENT_INPUT, viewModel.currentInput.value)
        prefs.putString(KEY_FIRST_OPERAND, viewModel.firstOperand?.toString())
        prefs.putString(KEY_PENDING_OPERATION, viewModel.pendingOperation)
        prefs.putBoolean(KEY_WAITING_FOR_SECOND, viewModel.waitingForSecondOperand)
        prefs.apply()
    }

    private fun restoreCalculatorState() {
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
}