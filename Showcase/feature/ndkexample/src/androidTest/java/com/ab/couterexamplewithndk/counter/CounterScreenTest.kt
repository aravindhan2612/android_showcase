package com.ab.couterexamplewithndk.counter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.ab.counterexamplewithndk.R
import com.ab.couterexamplewithndk.domain.model.CounterModel
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.CounterScreen
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.CounterUiIntent
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.CounterUiState
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.CounterViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CounterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: CounterViewModel
    private lateinit var stateFlow: MutableStateFlow<CounterUiState>
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
        stateFlow = MutableStateFlow(CounterUiState())
        every { viewModel.state } returns stateFlow
        composeTestRule.setContent {
            CounterScreen(counterViewModel = viewModel)
        }
    }

    @Test
    fun screenDisplaysAppTitle() {
        composeTestRule.onNodeWithText(context.getString(R.string.counter_app)).assertIsDisplayed()
    }

    @Test
    fun incrementButtonIsDisplayedAndEnabled_whenNotLoading() {
        composeTestRule.onNodeWithText(context.getString(R.string.increment))
            .assertIsDisplayed()
            .assertIsEnabled()
    }
    @Test
    fun incrementButtonIsDisabled_whenLoading() {
        stateFlow.value = CounterUiState(isLoading = true)
        composeTestRule.onNodeWithText(context.getString(R.string.increment)).assertDoesNotExist()
    }

    @Test
    fun clickingIncrementButton_callsViewModelIntent() {
        composeTestRule.onNodeWithText(context.getString(R.string.increment)).performClick()
        verify { viewModel.onIntent(CounterUiIntent.IncrementCount) }
    }

    @Test
    fun resetButtonIsDisabled_whenCountListIsEmpty() {
        stateFlow.value = CounterUiState(countList = emptyList())
        composeTestRule.onNodeWithText(context.getString(R.string.reset)).assertIsNotEnabled()
    }

    @Test
    fun resetButtonIsEnabled_whenCountListIsNotEmpty() {
        val historyList = listOf(
            CounterModel(1, System.currentTimeMillis()),
            CounterModel(2, System.currentTimeMillis())
        )
        stateFlow.value = CounterUiState(countList = historyList)

        composeTestRule.onNodeWithText(context.getString(R.string.reset))
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun clickingResetButton_callsViewModelIntent() {
        val historyList = listOf(CounterModel(1, System.currentTimeMillis()))
        stateFlow.value = CounterUiState(countList = historyList)
        composeTestRule.onNodeWithText(context.getString(R.string.reset)).performClick()
        verify { viewModel.onIntent(CounterUiIntent.ResetCount) }
    }

    @Test
    fun totalCountCardIsDisplayed_whenCountIsGreaterThanZero() {
        stateFlow.value = CounterUiState(currentCount = 5)
        composeTestRule.onNodeWithText(context.getString(R.string.total_count)).assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun countHistoryItemsAreDisplayed() {
        val timestamp = System.currentTimeMillis()
        val historyList = listOf(
            CounterModel(1, timestamp),
            CounterModel(2, timestamp + 1000),
            CounterModel(3, timestamp + 2000)
        )
        stateFlow.value = CounterUiState(countList = historyList, currentCount = 3)
        composeTestRule.onNodeWithText("Count: 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Count: 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Count: 3").assertIsDisplayed()
    }
}