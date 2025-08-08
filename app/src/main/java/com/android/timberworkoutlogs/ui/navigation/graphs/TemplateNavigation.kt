package com.android.timberworkoutlogs.ui.navigation.graphs

import androidx.compose.animation.ExitTransition
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.timberworkoutlogs.ui.elements.MainLayout
import com.android.timberworkoutlogs.ui.navigation.AppDestinations
import com.android.timberworkoutlogs.ui.navigation.homeComposable
import com.android.timberworkoutlogs.ui.navigation.slideComposable
import com.android.timberworkoutlogs.ui.screen.templates.CreateTemplateScreen
import com.android.timberworkoutlogs.ui.screen.templates.CreateWorkoutTemplateViewModel
import com.android.timberworkoutlogs.ui.screen.templates.TemplatesScreen
import com.android.timberworkoutlogs.ui.screen.templates.WorkoutTemplatesListScreen
import com.android.timberworkoutlogs.ui.screen.templates.WorkoutTemplatesViewModel
import java.util.UUID

fun NavGraphBuilder.templateGraph(navController: NavController) {
    homeComposable(AppDestinations.TEMPLATES_ROUTE) {
        MainLayout(navController = navController) {
            TemplatesScreen(
                onNavigateToExercisesList = {
                    navController.navigate(AppDestinations.EXERCISES_LIST_ROUTE)
                },
                onNavigateToWorkoutTemplatesList = {
                    navController.navigate(AppDestinations.TEMPLATES_LIST_ROUTE)
                }
            )
        }
    }

    slideComposable(AppDestinations.TEMPLATES_LIST_ROUTE) {
        val viewModel: WorkoutTemplatesViewModel = hiltViewModel()
        WorkoutTemplatesListScreen(
            viewModel = viewModel,
            onNavigateToCreateTemplate = { navController.navigate(AppDestinations.CREATE_TEMPLATE_ROUTE) },
            onNavigateToEditTemplate = { templateId ->
                navController.navigate("${AppDestinations.CREATE_TEMPLATE_ROUTE}?templateId=$templateId")
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    slideComposable(
        route = "${AppDestinations.CREATE_TEMPLATE_ROUTE}?templateId={templateId}",
        arguments = listOf(navArgument("templateId") {
            type = NavType.LongType
            defaultValue = -1L // Use -1 as the default value for "create" mode
        }),
        exitTransition = {
            if (targetState.destination.route?.startsWith(AppDestinations.WORKOUT_ROUTE) == true) {
                ExitTransition.None
            } else {
                slideOutOfContainer(
                    androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = androidx.compose.animation.core.tween(300)
                )
            }
        }
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
            },
            onStartWorkout = { workoutId ->
                navController.navigate(
                    "${AppDestinations.WORKOUT_ROUTE}?workoutId=$workoutId",
                    navOptions = NavOptions.Builder().setExitAnim(0).build()
                )
            }
        )
    }

}
