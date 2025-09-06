package com.android.timberworkoutlogs.ui.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.android.timberworkoutlogs.ui.navigation.AppDestinations
import com.android.timberworkoutlogs.ui.navigation.workoutComposable
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutScreen
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutViewModel
import com.android.timberworkoutlogs.ui.screen.workout.components.PlateCalculatorDialog
import java.util.UUID

private val workoutInitRoutes = setOf(
    AppDestinations.HOME_ROUTE,
    AppDestinations.CREATE_TEMPLATE_ROUTE,
    AppDestinations.TEMPLATES_LIST_ROUTE
)

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.workoutGraph(navController: NavController) {
    workoutComposable(
        route = AppDestinations.WORKOUT_ROUTE,
        exitTransition = {
            if (workoutInitRoutes.any { targetState.destination.route?.startsWith(it) == true }) {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(300)
                )
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            }
        },
        popExitTransition = {
            if (workoutInitRoutes.any { targetState.destination.route?.startsWith(it) == true }) {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(300)
                )
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            }
        }
    ) { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(AppDestinations.HOME_ROUTE)
        }
        val workoutViewModel: WorkoutViewModel = hiltViewModel(parentEntry)
        var showPlateCalculator by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            onOpenPlateCalculator = { showPlateCalculator = true }
        )

        if (showPlateCalculator) {
            ModalBottomSheet(
                onDismissRequest = { showPlateCalculator = false },
                sheetState = sheetState
            ) {
                PlateCalculatorDialog()
            }
        }
    }
}
