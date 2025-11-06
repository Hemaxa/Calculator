//класс, отвечающий за управление приложением

package com.example.calculator
import com.example.calculator.ui.theme.CalculatorAppTheme
import com.example.calculator.ui.theme.CalculatorThemeName

import android.content.Context //библиотека для работы с SharedPreferences
import android.os.Bundle //класс передачи данных между компонентами
import androidx.activity.ComponentActivity //класс активности в Android, поддерживающий Jetpack Compose
import androidx.activity.compose.setContent //главная функция, которая "включает" Jetpack Compose
import androidx.activity.viewModels //позволяет управлять состоянием данных, независи от жизненного цикла активностей
import androidx.compose.runtime.getValue //геттеры для получения значений состояний в Jetpack Compose
import androidx.compose.runtime.mutableStateOf //позволяет отслеживать состояния компонентов в Jetpack Compose
import androidx.compose.runtime.setValue //сеттеры для состояний в Jetpack Compose

class MainActivity : ComponentActivity() {

    //все статические поля класса, необходимые для SharedPreferences
    companion object {
        private const val PREFS_NAME = "CalculatorPrefs"
        private const val KEY_CURRENT_INPUT = "currentInput"
        private const val KEY_FIRST_OPERAND = "firstOperand"
        private const val KEY_PENDING_OPERATION = "pendingOperation"
        private const val KEY_WAITING_FOR_SECOND = "waitingForSecondOperand"
        private const val KEY_CURRENT_THEME = "currentTheme"
    }

    //инициализация viewModel, которая автоматически создает (или загружает) CalculatorViewModel
    private val viewModel: CalculatorViewModel by viewModels()

    //создание переменной хранения темы currentTheme (при изменении темы Jetpack Compose все автоматически перерисует все необходимое)
    private var currentTheme by mutableStateOf(CalculatorThemeName.Blue)

    //метод onCreate вызывается при запуске приложения или повороте экрана
    override fun onCreate(savedInstanceState: Bundle?) {
        //вызов родительского метода
        super.onCreate(savedInstanceState)

        //загрузка темы
        currentTheme = loadSavedTheme()

        //if отработает, если это первый запуск приложения (проверка на поворот экрана)
        if (!viewModel.stateRestored) {
            restoreCalculatorState() //выгрузка состояний
            viewModel.stateRestored = true //установка служебного флага
        }

        //отрисовка приложения
        setContent {
            //подгрузка темы
            CalculatorAppTheme(themeName = currentTheme) {
                //построение главного экрана
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

    //метод onStop вызывается при закрытии или сворачивании приложения
    override fun onStop() {
        super.onStop()
        saveCalculatorState()
    }

    //метод загрузки сохраненной темы
    private fun loadSavedTheme(): CalculatorThemeName {
        //выгрузка всех настроек в prefs
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        //установка темы (по умолчанию применяется голубая тема)
        val themeName = prefs.getString(KEY_CURRENT_THEME, "Blue")

        //обработка различных тем
        return when (themeName) {
            "Orange" -> CalculatorThemeName.Orange
            "Pink" -> CalculatorThemeName.Pink
            else -> CalculatorThemeName.Blue
        }
    }

    //метод сохранения выбранной темы
    private fun saveTheme(themeName: CalculatorThemeName) {
        //выгрузка всех настроек в prefs с возможностью редактирования
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()

        //установка новой темы в поле KEY_CURRENT_THEME
        prefs.putString(KEY_CURRENT_THEME, themeName.name)

        //сохранение изменений
        prefs.apply()
    }

    //метод для полного сохранения всего состояния viewModel
    private fun saveCalculatorState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()

        //установка новых значений в соответствующие поля
        prefs.putString(KEY_CURRENT_INPUT, viewModel.currentInput.value)
        prefs.putString(KEY_FIRST_OPERAND, viewModel.firstOperand?.toString())
        prefs.putString(KEY_PENDING_OPERATION, viewModel.pendingOperation)
        prefs.putBoolean(KEY_WAITING_FOR_SECOND, viewModel.waitingForSecondOperand)

        prefs.apply()
    }

    //метод для полного восстановления всего состояния viewModel
    private fun restoreCalculatorState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        //введенное в поле значение (по умолчанию "0")
        viewModel.currentInput.value = prefs.getString(KEY_CURRENT_INPUT, "0")

        //чтение первого операнда
        val savedOperand = prefs.getString(KEY_FIRST_OPERAND, null)

        //если не null (был ввод)
        if (savedOperand != null) {
            //перевод значения из string в double
            viewModel.firstOperand = savedOperand.toDoubleOrNull()
        }
        else {
            viewModel.firstOperand = null
        }

        //чтение оператора и состояния ввода второго операнда
        viewModel.pendingOperation = prefs.getString(KEY_PENDING_OPERATION, null)
        viewModel.waitingForSecondOperand = prefs.getBoolean(KEY_WAITING_FOR_SECOND, false)
    }
}