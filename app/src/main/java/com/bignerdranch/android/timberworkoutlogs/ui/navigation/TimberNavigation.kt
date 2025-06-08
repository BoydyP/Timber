package com.bignerdranch.android.timberworkoutlogs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bignerdranch.android.timberworkoutlogs.ui.screen.HomeScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.NewWorkoutScreen

// Defines the routes for navigation
object AppDestinations {
    const val HOME_ROUTE = "home"
    const val NEW_WORKOUT_ROUTE = "new_workout"
    // Add other routes here e.g., STATS_ROUTE, HISTORY_ROUTE
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE
    ) {
        composable(AppDestinations.HOME_ROUTE) {
            // Pass the navController to HomeScreen so it can navigate
            HomeScreen(navController = navController)
        }
        composable(AppDestinations.NEW_WORKOUT_ROUTE) {
            NewWorkoutScreen(
                onFinishWorkout = {
                    // Navigate back to the home screen when workout is finished
                    navController.popBackStack()
                },
                onOpenNotes = {},
                onUpdateNotes = {},
                onOpenPlateCalculator = {}
            )
        }
        // TODO: Add composable destinations for Stats, History, etc.
    }
}
