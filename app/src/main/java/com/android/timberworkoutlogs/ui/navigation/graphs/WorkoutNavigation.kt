package com.android.timberworkoutlogs.ui.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.android.timberworkoutlogs.ui.navigation.AppDestinations
import com.android.timberworkoutlogs.ui.navigation.workoutComposable
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutScreen
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutViewModel
import java.util.UUID

fun NavGraphBuilder.workoutGraph(navController: NavController) {
    workoutComposable(AppDestinations.WORKOUT_ROUTE) { backStackEntry ->
        val workoutViewModel: WorkoutViewModel = hiltViewModel()

        val selectedId = backStackEntry.savedStateHandle.get<String>("selected_exercise_id")
        val exerciseIndex = backStackEntry.savedStateHandle.get<Int>("exercise_index")
        if (selectedId != null && exerciseIndex != null) {
            workoutViewModel.onExerciseSelected(exerciseIndex, UUID.fromString(selectedId))
            backStackEntry.savedStateHandle.remove<String>("selected_exercise_id")
            backStackEntry.savedStateHandle.remove<Int>("exercise_index")
        }
        WorkoutScreen(
            workoutViewModel = workoutViewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSelectExercise = { index ->
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "exercise_index",
                    index
                )
                navController.navigate(AppDestinations.SELECT_EXERCISE_ROUTE)
            },
            onOpenNotes = { /* TODO */ },
            onOpenPlateCalculator = { /* TODO */ }
        )
    }
}