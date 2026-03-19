package com.ab.couterexamplewithndk.presentation.ui.screens.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ab.couterexamplewithndk.domain.usecase.GetCountHistoryUseCase
import com.ab.couterexamplewithndk.domain.usecase.IncrementCountUseCase
import com.example.counterapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CounterViewModel @Inject constructor(
    private val incrementCountUseCase: IncrementCountUseCase,
    private val getCountHistoryUseCase: GetCountHistoryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CounterUiState())
    val state = _state.onStart {
        observeHistory()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CounterUiState()
    )

    private fun observeHistory() {
        viewModelScope.launch {
            getCountHistoryUseCase.history.collect { history ->
                _state.update {
                    it.copy(
                        countList = history,
                        currentCount = history.lastOrNull()?.count ?: 0
                    )
                }
            }
        }
    }

    fun onIntent(intent: CounterUiIntent) {
        when (intent) {
            CounterUiIntent.GetCurrentCount -> TODO()
            CounterUiIntent.IncrementCount -> {
                incrementCounter()
            }

            CounterUiIntent.ResetCount -> {
                clearCount()
            }
        }
    }

    private fun clearCount() {
        viewModelScope.launch {
            getCountHistoryUseCase.clearHistory()
        }
    }

    private fun incrementCounter() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = incrementCountUseCase()) {
                is Resource.Success -> {
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                        )
                }

                is Resource.Error -> {
                    _state.value =
                        _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                }

            }
        }
    }
}