package com.example.calculator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    // Используем LiveData, чтобы MainActivity могла "наблюдать"
    // за изменениями и автоматически обновлять экран.
    val currentInput = MutableLiveData<String>("0")

    var firstOperand: Double? = null
    var pendingOperation: String? = null
    var waitingForSecondOperand = false

    // Флаг, чтобы мы загрузили данные из SharedPreferences только один раз
    var stateRestored = false
}