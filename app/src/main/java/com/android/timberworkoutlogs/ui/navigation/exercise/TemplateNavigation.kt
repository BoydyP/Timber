package com.android.timberworkoutlogs.ui.navigation.exercise

import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.timberworkoutlogs.ui.elements.MainLayout
import com.android.timberworkoutlogs.ui.navigation.AppDestinations
import com.android.timberworkoutlogs.ui.navigation.homeComposable
import com.android.timberworkoutlogs.ui.navigation.slideComposable
import com.android.timberworkoutlogs.ui.screen.TemplatesScreen
import com.android.timberworkoutlogs.ui.screen.templates.CreateTemplateScreen
import com.android.timberworkoutlogs.ui.screen.templates.CreateWorkoutTemplateViewModel
import com.android.timberworkoutlogs.ui.screen.templates.WorkoutTemplatesListScreen
import com.android.timberworkoutlogs.ui.screen.templates.WorkoutTemplatesViewModel
import java.util.UUID

fun NavGraphBuilder.templateGraph(navController: NavController) {
    homeComposable(AppDestinations.TEMPLATES_ROUTE) {
        MainLayout(navController = navController) {
            TemplatesScreen(
                onNavigateToExercisesList = { navController.navigate(AppDestinations.EXERCISES_LIST_ROUTE) },
                onNavigateToWorkoutTemplatesList = {
                    Log.d(
                        "TimberUiTemplatesLambda",
                        "Workout click received in onNavigateToWorkoutTemplatesList"
                    )
                    navController.navigate(AppDestinations.WORKOUT_TEMPLATES_LIST_ROUTE)
                }
            )
        }
    }
    slideComposable(AppDestinations.WORKOUT_TEMPLATES_LIST_ROUTE) {
        val viewModel: WorkoutTemplatesViewModel = hiltViewModel()
        Log.d("TimberUiTemplatesDestination", "Navigating to WorkoutTemplatesListScreen")
        WorkoutTemplatesListScreen(
            viewModel = viewModel,
            onNavigateToCreateTemplate = { navController.navigate(AppDestinations.CREATE_TEMPLATE_ROUTE) },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    slideComposable(
        route = "${AppDestinations.CREATE_TEMPLATE_ROUTE}?templateId={templateId}",
        arguments = listOf(navArgument("templateId") {
            type = NavType.StringType
            nullable = true
        })
    ) { backStackEntry ->
        val viewModel: CreateWorkoutTemplateViewModel = hiltViewModel()

        val selectedId = backStackEntry.savedStateHandle.get<String>("selected_exercise_id")
        val exerciseIndex = backStackEntry.savedStateHandle.get<Int>("exercise_index")
        if (selectedId != null && exerciseIndex != null) {
            viewModel.onExerciseSelected(exerciseIndex, UUID.fromString(selectedId))
            backStackEntry.savedStateHandle.remove<String>("selected_exercise_id")
            backStackEntry.savedStateHandle.remove<Int>("exercise_index")
        }

        CreateTemplateScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSelectExercise = { index ->
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "exercise_index",
                    index
                )
                navController.navigate(AppDestinations.SELECT_EXERCISE_ROUTE)
            }
        )
    }

}