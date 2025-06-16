package com.bignerdranch.android.timberworkoutlogs.ui

import TimberBottomNavigationBar
import TimberTopAppBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bignerdranch.android.timberworkoutlogs.TimberApplication
import com.bignerdranch.android.timberworkoutlogs.ui.screen.HistoryScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.HomeScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.SettingsScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.StatsScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.TemplatesScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.WorkoutScreen
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.WorkoutViewModel
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.WorkoutViewModelFactory

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val STATS_ROUTE = "stats"
    const val HISTORY_ROUTE = "history"
    const val WORKOUT_ROUTE = "workout"
    const val TEMPLATES_ROUTE = "templates"
    const val SETTINGS_ROUTE = "settings"
}

@Composable
fun TimberUi() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreenRoutes = setOf(
        AppDestinations.HOME_ROUTE,
        AppDestinations.STATS_ROUTE,
        AppDestinations.HISTORY_ROUTE,
        AppDestinations.TEMPLATES_ROUTE,
        AppDestinations.SETTINGS_ROUTE
    )

    Scaffold(
        topBar = {
            if (currentRoute in mainScreenRoutes) {
                TimberTopAppBar(onLanguageClick = { /* TODO */ })
            }
        },
        bottomBar = {
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
            // FIX: Conditionally apply padding. The WorkoutScreen should not have padding,
            // allowing it to draw over the entire screen area.
            modifier = if (currentRoute in mainScreenRoutes) {
                Modifier.padding(innerPadding)
            } else {
                Modifier
            }
        ) {
            composable(AppDestinations.HOME_ROUTE) {
                HomeScreen()
            }
            composable(AppDestinations.STATS_ROUTE) {
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
                val application = LocalContext.current.applicationContext as TimberApplication
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModelFactory(application.workoutRepository)
                )
                WorkoutScreen(
                    viewModel = workoutViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenNotes = { /* TODO */ },
                    onOpenPlateCalculator = { /* TODO */ }
                )
            }
        }
    }
}
