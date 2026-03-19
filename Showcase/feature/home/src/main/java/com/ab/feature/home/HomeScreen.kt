package com.ab.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ab.core.common.Route


@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel<HomeViewModel>(),
    navigate: (Route) -> Unit
) {
    LaunchedEffect(Unit) {
        homeViewModel.effectChannel.collect { route ->
            navigate(route)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize().padding(16.dp).systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Android ShowCase",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(demoDetails) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        homeViewModel.onIntent(HomeIntent.OnItemClicked(it.route))
                    }
                ) {
                    Text(
                        text = it.name,
                        maxLines = 1
                    )
                }
            }
        }
    }
}