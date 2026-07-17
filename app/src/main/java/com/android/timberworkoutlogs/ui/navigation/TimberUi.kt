package com.android.timberworkoutlogs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.android.timberworkoutlogs.ui.navigation.graphs.exerciseGraph
import com.android.timberworkoutlogs.ui.navigation.graphs.homeGraph
import com.android.timberworkoutlogs.ui.navigation.graphs.templateGraph
import com.android.timberworkoutlogs.ui.navigation.graphs.workoutGraph

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val STATS_ROUTE = "stats"
    const val HISTORY_ROUTE = "history"
    const val HISTORY_DETAIL_ROUTE = "history_detail"
    const val WORKOUT_ROUTE = "workout"
    const val TEMPLATES_ROUTE = "templates"
    const val SETTINGS_ROUTE = "settings"
    const val EXERCISES_LIST_ROUTE = "exercises_list"
    const val CREATE_EXERCISE_ROUTE = "create_exercise"
    const val SELECT_EXERCISE_ROUTE = "select_exercise"
    const val TEMPLATES_LIST_ROUTE = "workout_templates_list"
    const val CREATE_TEMPLATE_ROUTE = "create_template"
}

@Composable
fun TimberUi() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE,
    ) {
        homeGraph(navController)
        exerciseGraph(navController)
        templateGraph(navController)
        workoutGraph(navController)
    }
}
