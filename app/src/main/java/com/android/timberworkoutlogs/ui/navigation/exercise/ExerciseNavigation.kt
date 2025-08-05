package com.android.timberworkoutlogs.ui.navigation.exercise

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.timberworkoutlogs.ui.navigation.AppDestinations
import com.android.timberworkoutlogs.ui.navigation.slideComposable
import com.android.timberworkoutlogs.ui.screen.exercise.CreateExerciseScreen
import com.android.timberworkoutlogs.ui.screen.exercise.CreateExerciseViewModel
import com.android.timberworkoutlogs.ui.screen.exercise.ExerciseListScreen
import com.android.timberworkoutlogs.ui.screen.exercise.ExercisesListViewModel
import com.android.timberworkoutlogs.ui.screen.exercise.SelectExerciseScreen
import com.android.timberworkoutlogs.ui.screen.exercise.SelectExerciseViewModel

fun NavGraphBuilder.exerciseGraph(navController: NavController) {
    slideComposable(AppDestinations.EXERCISES_LIST_ROUTE) {
        val viewModel: ExercisesListViewModel = hiltViewModel()
        ExerciseListScreen(
            viewModel = viewModel,
            onNavigateToEditExercise = { exerciseId ->
                navController.navigate("${AppDestinations.CREATE_EXERCISE_ROUTE}?exerciseId=$exerciseId")
            },
            onNavigateToCreateExercise = { navController.navigate(AppDestinations.CREATE_EXERCISE_ROUTE) },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    slideComposable(
        route = "${AppDestinations.CREATE_EXERCISE_ROUTE}?exerciseId={exerciseId}",
        arguments = listOf(navArgument("exerciseId") {
            type = NavType.StringType
            nullable = true
        })
    ) {
        val viewModel: CreateExerciseViewModel = hiltViewModel()
        CreateExerciseScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
        )
    }

    slideComposable(AppDestinations.SELECT_EXERCISE_ROUTE) {
        val viewModel: SelectExerciseViewModel = hiltViewModel()
        SelectExerciseScreen(
            viewModel = viewModel,
            onExerciseSelected = { definitionId ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("selected_exercise_id", definitionId.toString())
                navController.popBackStack()
            },
            onNavigateToCreateExercise = { navController.navigate(AppDestinations.CREATE_EXERCISE_ROUTE) },
            onNavigateBack = { navController.popBackStack() }
        )
    }

}