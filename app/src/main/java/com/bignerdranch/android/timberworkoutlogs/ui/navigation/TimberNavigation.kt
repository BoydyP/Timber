package com.bignerdranch.android.timberworkoutlogs.ui.navigation

import TimberBottomNavigationBar
import TimberTopAppBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bignerdranch.android.timberworkoutlogs.ui.screen.HistoryScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.HomeScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.SettingsScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.StatsScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.TemplatesScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.WorkoutScreen

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val STATS_ROUTE = "stats" // Added a dedicated route for stats
    const val HISTORY_ROUTE = "history"
    const val WORKOUT_ROUTE = "workout"
    const val TEMPLATES_ROUTE = "templates"
    const val SETTINGS_ROUTE = "settings"
}

@Composable
fun TimberApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define the routes that show the main scaffold with top/bottom bars
    val mainScreenRoutes = setOf(
        AppDestinations.HOME_ROUTE,
        AppDestinations.STATS_ROUTE, // Added Stats to the main screens
        AppDestinations.HISTORY_ROUTE,
        AppDestinations.TEMPLATES_ROUTE,
        AppDestinations.SETTINGS_ROUTE
    )

    Scaffold(
        topBar = {
            // Only show the top app bar on the main screens
            if (currentRoute in mainScreenRoutes) {
                TimberTopAppBar(onLanguageClick = { /* TODO */ })
            }
        },
        bottomBar = {
            // Only show the bottom navigation bar on the main screens
            if (currentRoute in mainScreenRoutes) {
                TimberBottomNavigationBar(
                    currentRoute = currentRoute,
                    onItemSelected = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.HOME_ROUTE) {
                HomeScreen()
            }
            composable(AppDestinations.STATS_ROUTE) { // Added composable for StatsScreen
                StatsScreen()
            }
            composable(AppDestinations.HISTORY_ROUTE) {
                HistoryScreen()
            }
            composable(AppDestinations.TEMPLATES_ROUTE) {
                TemplatesScreen()
            }
            composable(AppDestinations.SETTINGS_ROUTE) {
                SettingsScreen()
            }
            composable(AppDestinations.WORKOUT_ROUTE) {
                WorkoutScreen(
                    onFinishWorkout = { navController.popBackStack() },
                    onOpenNotes = { },
                    onDiscardWorkout = { navController.popBackStack() },
                    onOpenPlateCalculator = { }
                )
            }
        }
    }
}
