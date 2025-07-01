package com.android.timberworkoutlogs.ui

import TimberBottomNavigationBar
import TimberTopAppBar
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.timberworkoutlogs.TimberApplication
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
//    const val WORKOUT_TEMPLATES_ROUTE = "workout_templates"
}

@Composable
fun TimberUi() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screens that provide their own Scaffold.
    val screensWithCustomScaffold = setOf(
        "${AppDestinations.CREATE_EXERCISE_ROUTE}?exerciseId={exerciseId}",
        AppDestinations.SELECT_EXERCISE_ROUTE,
        AppDestinations.WORKOUT_ROUTE
    )

    if (currentRoute in screensWithCustomScaffold) {
        // Just render the NavHost, the screen inside will have its own Scaffold
        TimberNavHost(navController = navController, modifier = Modifier)
    } else {
        // Render the main Scaffold for all other screens
        MainScaffold(navController = navController, currentRoute = currentRoute)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(navController: NavHostController, currentRoute: String?) {
    val mainScreenRoutes = setOf(
        AppDestinations.HOME_ROUTE,
        AppDestinations.STATS_ROUTE,
        AppDestinations.HISTORY_ROUTE,
        AppDestinations.TEMPLATES_ROUTE,
        AppDestinations.SETTINGS_ROUTE
    )

    Scaffold(
        topBar = {
            when (currentRoute) {
                in mainScreenRoutes -> {
                    TimberTopAppBar(onIconClick = { /* TODO */ })
                }

                AppDestinations.EXERCISES_LIST_ROUTE -> {
                    TopAppBar(
                        title = { Text("Exercise Library") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
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
        },
        floatingActionButton = {
            if (currentRoute == AppDestinations.EXERCISES_LIST_ROUTE) {
                FloatingActionButton(onClick = { navController.navigate(AppDestinations.CREATE_EXERCISE_ROUTE) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Create new exercise")
                }
            }
        }
    ) { innerPadding ->
        TimberNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
private fun TimberNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val application = LocalContext.current.applicationContext as TimberApplication

    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE,
        modifier = modifier
    ) {
        composable(AppDestinations.HOME_ROUTE) {
            HomeScreen()
        }
        composable(AppDestinations.STATS_ROUTE) {
            StatsScreen()
        }
        composable(AppDestinations.HISTORY_ROUTE) {
            val viewModel: WorkoutHistoryViewModel =
                viewModel(factory = WorkoutHistoryViewModelFactory(application.workoutRepository))
            WorkoutHistoryScreen(
                viewModel = viewModel,
                onNavigateToWorkout = {}
            )
        }
        composable(AppDestinations.TEMPLATES_ROUTE) {
            TemplatesScreen(
                onNavigateToExercisesList = { navController.navigate(AppDestinations.EXERCISES_LIST_ROUTE) }
            )
        }
        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen()
        }

        composable(AppDestinations.EXERCISES_LIST_ROUTE) {
            val viewModel: ExercisesListViewModel =
                viewModel(factory = ExercisesListViewModelFactory(application.exerciseDefinitionRepository))
            ExerciseListScreen(
                viewModel = viewModel,
                contentPadding = PaddingValues(),
                onNavigateToEditExercise = { exerciseId ->
                    navController.navigate("${AppDestinations.CREATE_EXERCISE_ROUTE}?exerciseId=$exerciseId")
                }
            )
        }

        composable(
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

        composable(AppDestinations.SELECT_EXERCISE_ROUTE) {
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

        composable(AppDestinations.WORKOUT_ROUTE) { backStackEntry ->
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
