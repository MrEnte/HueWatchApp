package com.example.android.wearable.composestarter.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

@Composable
fun Navigation() {
    val navController = rememberSwipeDismissableNavController()
    SwipeDismissableNavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            WearApp(navController)
        }
        composable(route = Screen.DetailScreen.route + "/{roomId}", arguments = listOf(
            navArgument("roomId") {
                type = NavType.StringType
            }
        )){ entry ->
            entry.arguments?.getString("roomId")?.let { RoomSettings(roomId = it) }
        }
    }
}