package com.ab.couterexamplewithndk.presentation.ui.screens.counter

import com.ab.couterexamplewithndk.domain.model.CounterModel

data class CounterUiState(
    val currentCount: Int = 0,
    val countList: List<CounterModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
