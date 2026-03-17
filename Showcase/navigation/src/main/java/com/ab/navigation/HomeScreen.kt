package com.ab.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    navToMaterial3Expressive: () -> Unit,
    navToNDKExample: () -> Unit,
    navToChatExample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Android ShowCase",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = navToMaterial3Expressive
        ) {
            Text(text = "Go to Material3 Expressive")
        }

        Button(
            onClick = navToNDKExample
        ) {
            Text(text = "Go to NDKCounterApp")
        }

        Button(
            onClick = navToChatExample
        ) {
            Text(text = "Go to ChatExample")
        }
    }
}