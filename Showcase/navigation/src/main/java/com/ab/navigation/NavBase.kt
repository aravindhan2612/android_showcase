package com.ab.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ab.chatexample.ChatScreen
import com.ab.couterexamplewithndk.presentation.ui.screens.counter.CounterScreen
import com.ab.feature.home.screen.HomeScreen
import com.ab.material3expressive.screens.Material3ExpressiveHomeScreen

@Composable
fun NavBase() {
    val backStack = rememberNavBackStack(Route.Home)

    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Route.Home> {
                HomeScreen(
                    navToMaterial3Expressive = {
                        backStack.add(Route.Material3Expressive)
                    },
                    navToNDKExample = {
                        backStack.add(Route.NDKExample)
                    },
                    navToChatExample = {
                        backStack.add(Route.ChatExample)
                    }
                )
            }
            entry<Route.Material3Expressive> {
                Material3ExpressiveHomeScreen()
            }
            entry<Route.NDKExample> {
                CounterScreen()
            }

            entry<Route.ChatExample> {
                ChatScreen()
            }
        }
    )
}