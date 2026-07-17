package com.android.timberworkoutlogs.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.timberworkoutlogs.ui.elements.MainLayout
import com.android.timberworkoutlogs.ui.navigation.AppDestinations
import com.android.timberworkoutlogs.ui.navigation.homeComposable
import com.android.timberworkoutlogs.ui.navigation.slideComposable
import com.android.timberworkoutlogs.ui.screen.home.HomeScreen
import com.android.timberworkoutlogs.ui.screen.stats.StatsScreen
import com.android.timberworkoutlogs.ui.screen.settings.SettingsScreen
import com.android.timberworkoutlogs.ui.screen.settings.SettingsViewModel
import com.android.timberworkoutlogs.ui.screen.history.WorkoutHistoryDetailScreen
import com.android.timberworkoutlogs.ui.screen.history.WorkoutHistoryScreen
import com.android.timberworkoutlogs.ui.screen.history.WorkoutHistoryViewModel

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
                onNavigateToWorkout = { workoutId ->
                    navController.navigate("${AppDestinations.HISTORY_DETAIL_ROUTE}?workoutId=$workoutId")
                }
            )
        }
    }
    slideComposable(
        route = "${AppDestinations.HISTORY_DETAIL_ROUTE}?workoutId={workoutId}",
        arguments = listOf(navArgument("workoutId") {
            type = NavType.LongType
            defaultValue = -1L
        })
    ) {
        WorkoutHistoryDetailScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
    homeComposable(AppDestinations.SETTINGS_ROUTE) {
        MainLayout(navController = navController) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(viewModel = viewModel)
        }
    }
}
