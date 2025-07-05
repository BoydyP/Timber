package com.android.timberworkoutlogs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.timberworkoutlogs.TimberApplication
import com.android.timberworkoutlogs.ui.elements.MainLayout
import com.android.timberworkoutlogs.ui.screen.HomeScreen
import com.android.timberworkoutlogs.ui.screen.SettingsScreen
import com.android.timberworkoutlogs.ui.screen.StatsScreen
import com.android.timberworkoutlogs.ui.screen.TemplatesScreen
import com.android.timberworkoutlogs.ui.screen.exercise.CreateExerciseScreen
import com.android.timberworkoutlogs.ui.screen.exercise.CreateExerciseViewModel
import com.android.timberworkoutlogs.ui.screen.exercise.ExerciseListScreen
import com.android.timberworkoutlogs.ui.screen.exercise.ExercisesListViewModel
import com.android.timberworkoutlogs.ui.screen.exercise.ExercisesListViewModelFactory
import com.android.timberworkoutlogs.ui.screen.exercise.SelectExerciseScreen
import com.android.timberworkoutlogs.ui.screen.exercise.SelectExerciseViewModel
import com.android.timberworkoutlogs.ui.screen.exercise.SelectExerciseViewModelFactory
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutHistoryScreen
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutHistoryViewModel
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutHistoryViewModelFactory
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutScreen
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutViewModel
import com.android.timberworkoutlogs.ui.screen.workout.WorkoutViewModelFactory
import java.util.UUID

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val STATS_ROUTE = "stats"
    const val HISTORY_ROUTE = "history"
    const val WORKOUT_ROUTE = "workout"
    const val TEMPLATES_ROUTE = "templates"
    const val SETTINGS_ROUTE = "settings"
    const val EXERCISES_LIST_ROUTE = "exercises_list"
    const val CREATE_EXERCISE_ROUTE = "create_exercise"
    const val SELECT_EXERCISE_ROUTE = "select_exercise"
}

@Composable
fun TimberUi() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE,
    ) {
        homeComposable(AppDestinations.HOME_ROUTE) {
            MainLayout(navController = navController) {
                HomeScreen()
            }
        }
        homeComposable(AppDestinations.STATS_ROUTE) {
            MainLayout(navController = navController) {
                StatsScreen()
            }
        }
        homeComposable(AppDestinations.HISTORY_ROUTE) {
            MainLayout(navController = navController) {
                val application = LocalContext.current.applicationContext as TimberApplication
                val viewModel: WorkoutHistoryViewModel =
                    viewModel(factory = WorkoutHistoryViewModelFactory(application.workoutRepository))
                WorkoutHistoryScreen(
                    viewModel = viewModel,
                    onNavigateToWorkout = {}
                )
            }
        }
        homeComposable(AppDestinations.TEMPLATES_ROUTE) {
            MainLayout(navController = navController) {
                TemplatesScreen(
                    onNavigateToExercisesList = { navController.navigate(AppDestinations.EXERCISES_LIST_ROUTE) }
                )
            }
        }
        homeComposable(AppDestinations.SETTINGS_ROUTE) {
            MainLayout(navController = navController) {
                SettingsScreen()
            }
        }

        slideComposable(AppDestinations.EXERCISES_LIST_ROUTE) {
            val application = LocalContext.current.applicationContext as TimberApplication
            val viewModel: ExercisesListViewModel =
                viewModel(factory = ExercisesListViewModelFactory(application.exerciseDefinitionRepository))
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
            val viewModel: CreateExerciseViewModel = viewModel(
                factory = CreateExerciseViewModel.Factory
            )
            CreateExerciseScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        slideComposable(AppDestinations.SELECT_EXERCISE_ROUTE) {
            val application = LocalContext.current.applicationContext as TimberApplication
            val viewModel: SelectExerciseViewModel =
                viewModel(factory = SelectExerciseViewModelFactory(application.exerciseDefinitionRepository))
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

        slideComposable(AppDestinations.WORKOUT_ROUTE) { backStackEntry ->
            val application = LocalContext.current.applicationContext as TimberApplication
            val workoutViewModel: WorkoutViewModel = viewModel(
                factory = WorkoutViewModelFactory(
                    application.workoutRepository,
                    application.exerciseDefinitionRepository
                )
            )

            val selectedId = backStackEntry.savedStateHandle.get<String>("selected_exercise_id")
            val exerciseIndex = backStackEntry.savedStateHandle.get<Int>("exercise_index")
            if (selectedId != null && exerciseIndex != null) {
                workoutViewModel.onExerciseSelected(exerciseIndex, UUID.fromString(selectedId))
                backStackEntry.savedStateHandle.remove<String>("selected_exercise_id")
                backStackEntry.savedStateHandle.remove<Int>("exercise_index")
            }

            WorkoutScreen(
                viewModel = workoutViewModel,
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
}
