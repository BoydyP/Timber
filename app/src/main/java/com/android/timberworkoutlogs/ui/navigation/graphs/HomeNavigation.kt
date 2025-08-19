package com.android.timberworkoutlogs.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.android.timberworkoutlogs.ui.elements.MainLayout
import com.android.timberworkoutlogs.ui.navigation.AppDestinations
import com.android.timberworkoutlogs.ui.navigation.homeComposable
import com.android.timberworkoutlogs.ui.screen.HomeScreen
import com.android.timberworkoutlogs.ui.screen.StatsScreen
import com.android.timberworkoutlogs.ui.screen.settings.SettingsScreen
import com.android.timberworkoutlogs.ui.screen.settings.SettingsViewModel
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutHistoryScreen
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutHistoryViewModel

fun NavGraphBuilder.homeGraph(navController: NavController) {
    homeComposable(AppDestinations.HOME_ROUTE) {
        MainLayout(navController = navController) {
            HomeScreen(
                navigateToWorkout = { navController.navigate(AppDestinations.WORKOUT_ROUTE) }
            )
        }
    }
    homeComposable(AppDestinations.STATS_ROUTE) {
        MainLayout(navController = navController) {
            StatsScreen()
        }
    }
    homeComposable(AppDestinations.HISTORY_ROUTE) {
        MainLayout(navController = navController) {
            val viewModel: WorkoutHistoryViewModel = hiltViewModel()
            WorkoutHistoryScreen(
                viewModel = viewModel,
                onNavigateToWorkout = {}
            )
        }
    }
    homeComposable(AppDestinations.SETTINGS_ROUTE) {
        MainLayout(navController = navController) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(viewModel = viewModel)
        }
    }
}
