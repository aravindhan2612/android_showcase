package com.ab.couterexamplewithndk

import app.cash.turbine.test
import com.ab.couterexamplewithndk.domain.model.CounterModel
import com.ab.couterexamplewithndk.domain.usecase.GetCountHistoryUseCase
import com.ab.couterexamplewithndk.domain.usecase.IncrementCountUseCase
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.CounterUiIntent
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.CounterUiState
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.CounterViewModel
import com.example.counterapp.utils.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CounterViewModelTest {
    private lateinit var viewModel: CounterViewModel
    private lateinit var incrementCountUseCase: IncrementCountUseCase
    private lateinit var getCountHistoryUseCase: GetCountHistoryUseCase

    @Before
    fun setup() {
        incrementCountUseCase = mockk()
        getCountHistoryUseCase = mockk()
        viewModel = CounterViewModel(incrementCountUseCase, getCountHistoryUseCase)
    }

    @Test
    fun `initial state should be empty`() = runTest {
        val emptyHistory = emptyList<CounterModel>()
        every { getCountHistoryUseCase.history } returns flowOf(emptyHistory)
        viewModel.state.test {
            val emission = awaitItem()
            assertEquals(CounterUiState(), emission)
            Assert.assertEquals(0, emission.currentCount)
            assertEquals(emptyList<CounterModel>(), emission.countList)
        }
    }

    @Test
    fun `state should update with history when collected`() = runTest {
        val historyList = listOf(
            CounterModel(1, System.currentTimeMillis()),
            CounterModel(2, System.currentTimeMillis()),
            CounterModel(3, System.currentTimeMillis())
        )
        every { getCountHistoryUseCase.history } returns flowOf(historyList)
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(CounterUiState(), initialState)

            val updatedState = awaitItem()
            Assert.assertEquals(3, updatedState.currentCount)
            assertEquals(historyList, updatedState.countList)
            assertEquals(false, updatedState.isLoading)
        }
    }

    @Test
    fun `incrementCounter should update state on success`() = runTest {
        every { getCountHistoryUseCase.history } returns flowOf(emptyList())
        coEvery { incrementCountUseCase() } returns Resource.Success(0)
        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(CounterUiIntent.IncrementCount)
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)
            val successState = awaitItem()
            assertEquals(false, successState.isLoading)
            Assert.assertEquals(null, successState.error)
            coVerify(exactly = 1) { incrementCountUseCase() }
        }
    }

    @Test
    fun `incrementCounter should update state with error on failure`() = runTest {
        val errorMessage = "Failed to increment"
        every { getCountHistoryUseCase.history } returns flowOf(emptyList())
        coEvery { incrementCountUseCase() } returns Resource.Error(errorMessage)
        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(CounterUiIntent.IncrementCount)
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)
            val errorState = awaitItem()
            assertEquals(false, errorState.isLoading)
            assertEquals(errorMessage, errorState.error)
            coVerify(exactly = 1) { incrementCountUseCase() }
        }
    }

    @Test
    fun `resetCount should clear history`() = runTest {
        val historyList = listOf(CounterModel(5, System.currentTimeMillis()))
        every { getCountHistoryUseCase.history } returns flowOf(historyList)
        coEvery { getCountHistoryUseCase.clearHistory() } returns Unit
        viewModel.onIntent(CounterUiIntent.ResetCount)
        coVerify(exactly = 1) { getCountHistoryUseCase.clearHistory() }
    }

}