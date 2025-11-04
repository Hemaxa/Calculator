//класс, отвечающий за сохранение состояний (введенное число, введенная операция, настройка темы)

package com.example.calculator

import androidx.lifecycle.MutableLiveData //контейнер для данных
import androidx.lifecycle.ViewModel //класс для сохранения состояний данных

class CalculatorViewModel : ViewModel() {
    //контейнер введенной строки (по умолчанию "0")
    val currentInput = MutableLiveData<String>("0")

    //контейнер первого числа
    var firstOperand: Double? = null

    //контейнер операции
    var pendingOperation: String? = null

    //флаг, подтверждающий ввод операции
    var waitingForSecondOperand = false

    //служебный флаг для единоразовой загрузки настроек из SharedPreferences
    var stateRestored = false
}