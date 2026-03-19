package com.ab.couterexamplewithndk.presentation.ui.screens.counter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ab.counterexamplewithndk.R
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.components.TotalCountCardComponent
import com.example.counterapp.presentation.ui.screens.counter.components.CountHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    counterViewModel: CounterViewModel = hiltViewModel()
) {
    val state by counterViewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.counter_app),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TotalCountCardComponent(
                    state.currentCount,
                    state.isLoading
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Button(
                        onClick = {
                            counterViewModel.onIntent(CounterUiIntent.IncrementCount)
                        },
                        enabled = !state.isLoading,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(stringResource(R.string.increment))
                        }
                    }
                    OutlinedButton(
                        enabled = state.countList.isNotEmpty() && !state.isLoading,
                        onClick = {
                            counterViewModel.onIntent(CounterUiIntent.ResetCount)
                        },
                        modifier = Modifier
                            .padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                }
            }


            state.error?.let { error ->
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            AnimatedVisibility(
                visible = state.countList.isNotEmpty()
            ) {
                Text(
                    text = stringResource(R.string.count_history),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.countList,
                    key = { it.count }
                ) { item ->

                    CountHistoryItem(
                        count = item.count,
                        timestamp = item.timeStamp,
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}




