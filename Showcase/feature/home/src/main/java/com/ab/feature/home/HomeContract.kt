package com.ab.feature.home

import com.ab.core.common.Route

data class DemoDetail(
    val name: String = "",
    val route: Route = Route.Home
)

val demoDetails = listOf(
    DemoDetail("Material3Expressive Sample", Route.Material3Expressive),
    DemoDetail("NDK Sample", Route.NDKExample),
    DemoDetail("Chat Sample", Route.ChatExample)
)

sealed class HomeIntent() {
    data class OnItemClicked(val route: Route) : HomeIntent()
}