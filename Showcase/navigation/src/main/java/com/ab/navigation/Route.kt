package com.ab.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    object Material3Expressive : Route

    @Serializable
    object Home : Route

    @Serializable
    object NDKExample: Route

    @Serializable
    object ChatExample: Route
}