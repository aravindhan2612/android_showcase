package com.ab.couterexamplewithndk.presentation.ui.screens.counter

sealed interface CounterUiIntent {
    object IncrementCount : CounterUiIntent
    object GetCurrentCount : CounterUiIntent
    object ResetCount : CounterUiIntent
}