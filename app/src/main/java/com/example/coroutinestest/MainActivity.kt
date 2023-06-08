package com.example.coroutinestest

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutinestest.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //фиксированная портретная ориентация экрана
//--------------------------------------------------------------------------------------------------
        val scopeOne = CoroutineScope(Dispatchers.Default) //диспатчер Default для создания корутин
//--------------------------------------------------------------------------------------------------
// По нажатию кнопки "START", запустятся две coroutine, которые будут использовать потоки данных flow на диспатчере Default и создавать sharedFlow

        binding.startButton.setOnClickListener {
            var number = binding.putNumber.text.toString().toIntOrNull() ?: 0  //принимается введенное пользователем значение с проверкой на null
//--------------------------------------------------------------------------------------------------
//Таймер1
            val scopeForTimerOne = scopeOne.launch(Dispatchers.Default) {//корутина для таймера 1
                val numberOne = number //использование переменной number для timer1

                flowForTimer1(numberOne).collect { ticker ->
                    Log.i("Start1", "Это поток 1 -> ${Thread.currentThread().name}")
                    withContext(Dispatchers.Main) { //переключение на main поток для передачи данных в UI
                        binding.timer1.text = ticker.toString()
                    }
                }
            }
//--------------------------------------------------------------------------------------------------
//Таймер2
            val scopeForTimerTwo = scopeOne.launch(Dispatchers.Default) {//корутина для таймера 2
                val numberTwo = number.toBigDecimal() //использование переменной number для timer2. Для точности данных в дробном значении используется BigDecimal

                flowForTimer2(numberTwo).collect { ticker2 ->
                    Log.i("Start1", "Это поток 2 -> ${Thread.currentThread().name}")
                    withContext(Dispatchers.Main) {//переключение на main поток для передачи данных в UI
                        binding.timer2.text = ticker2.toEngineeringString()
                    }
                }
            }
//--------------------------------------------------------------------------------------------------
            if (scopeForTimerOne.isActive && scopeForTimerTwo.isActive) { //если корутна активна - кнопка старт не видна пользоваткелю. Появится при нажатии кнопки стоп.
                binding.startButton.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
            }
//--------------------------------------------------------------------------------------------------
// По нажатию кнопки CANCEL, обе coroutine останавливаются, проверяются условия и таймер обнуляется при повторном нажатии

            binding.cancelButton.setOnClickListener {
                scopeForTimerOne.cancel()
                Log.i("Cancel, timer1", "$scopeForTimerOne")

                scopeForTimerTwo.cancel()
                Log.i("Cancel, timer2", "$scopeForTimerTwo")

                if (scopeForTimerOne.isCancelled && scopeForTimerTwo.isCancelled || //проверка, если корутина отменена или выполнена то кнопка старт снова появится и значение счетчика станет 0
                    scopeForTimerOne.isCompleted && scopeForTimerTwo.isCompleted
                ) {
                    number = 0
                    binding.startButton.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.timer1.text = number.toString()
                    binding.timer2.text = number.toDouble().toString()
                }
            }
        }
    }
}

//поток данных flow для таймера1, отсчитывает время посекундно (1000мс), от изначально введенного значения, в порядке убывания до нуля
fun flowForTimer1(ticker: Int): Flow<Int> = flow {
    var ticker2 = ticker

    while (ticker2 > 0) {
        delay(1000L)
        ticker2--
        emit(ticker2)
    }
}.shareIn(CoroutineScope(Dispatchers.Default), SharingStarted.WhileSubscribed(), 0)

//поток данных flow для таймера2, отсчитывает время по одной десятой секунде (100 мс), отображая в порядке убывания от введеного значения до нуля. Округление дроби до первого знака
fun flowForTimer2(ticker2: BigDecimal): Flow<BigDecimal> = flow {//поток данных для таймера 2
    var ticker3 = ticker2

    while (ticker3 > BigDecimal(0.0)) {
        delay(100)
        ticker3 = ticker3.subtract(BigDecimal(0.1)).setScale(1, RoundingMode.HALF_EVEN)
        emit(ticker3)
    }
}.shareIn(CoroutineScope(Dispatchers.Default), SharingStarted.WhileSubscribed(), 0)

