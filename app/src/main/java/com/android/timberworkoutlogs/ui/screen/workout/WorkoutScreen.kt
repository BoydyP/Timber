package com.android.timberworkoutlogs.ui.screen.workout

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.services.TimerService
import com.android.timberworkoutlogs.ui.common.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.common.timerFeatureNotSupportedToast
import com.android.timberworkoutlogs.ui.screen.exercise.components.ExerciseInputCard
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutBottomActions
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutTopAppBar
import com.android.timberworkoutlogs.ui.theme.TimberOrange
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import java.util.UUID

@Composable
fun WorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSelectExercise: (exerciseIndex: Int) -> Unit,
    onOpenNotes: () -> Unit,
    onOpenPlateCalculator: () -> Unit
) {
    val workoutExercises = workoutViewModel.workoutExercises
    val exerciseDefinitions = workoutViewModel.exerciseDefinitions
    val isWorkoutEmpty by workoutViewModel.isWorkoutEmpty.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var timerService by remember { mutableStateOf<TimerService?>(null) }
    val timerText by timerService?.timerText?.collectAsState("") ?: remember { mutableStateOf("") }
    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else {
            mutableStateOf(true)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    val serviceConnection = remember {
        object : ServiceConnection {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as TimerService.TimerBinder
                timerService = binder.getService()
                if (hasNotificationPermission) {
                    timerService?.startTimer()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        timerFeatureNotSupportedToast(context)
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                timerService = null
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        Intent(context, TimerService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    BackHandler(enabled = true) {
        if (isWorkoutEmpty) {
            timerService?.stopTimer()
            workoutViewModel.onDiscardWorkout(onNavigateBack)
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            WorkoutTopAppBar(
                title = "Log Workout",
                timerText = timerText,
                onDiscardWorkout = {
                    timerService?.stopTimer()
                    workoutViewModel.onDiscardWorkout(onNavigateBack)
                },
            )
        },
        bottomBar = {
            WorkoutBottomActions(
                onOpenNotes = onOpenNotes,
                onFinishWorkout = {
                    val secondsElapsed = timerService?.getSecondsElapsed() ?: 0
                    timerService?.stopTimer()
                    workoutViewModel.onFinishWorkout(secondsElapsed, onNavigateBack)
                },
                isFinishEnabled = !isWorkoutEmpty,
                onOpenPlateCalculator = onOpenPlateCalculator
            )
        }
    ) { innerPadding ->
        WorkoutExerciseList(
            modifier = Modifier.padding(innerPadding),
            workoutExercises = workoutExercises,
            exerciseDefinitions = exerciseDefinitions,
            onAddSet = workoutViewModel::onAddSet,
            onDeleteExercise = workoutViewModel::deleteExercise,
            onDeleteSet = workoutViewModel::deleteSet,
            onSetChanged = workoutViewModel::onSetChanged,
            onExerciseUnitChange = workoutViewModel::onExerciseUnitChange,
            onAddExercise = workoutViewModel::onAddExercise,
            onNavigateToSelectExercise = onNavigateToSelectExercise
        )
    }
}

@Composable
private fun WorkoutExerciseList(
    modifier: Modifier = Modifier,
    workoutExercises: List<WorkoutExercise>,
    exerciseDefinitions: List<ExerciseDefinition?>,
    onAddSet: (UUID) -> Unit,
    onDeleteExercise: (WorkoutExercise) -> Unit,
    onDeleteSet: (UUID, ExerciseSet) -> Unit,
    onSetChanged: (UUID, Int, ExerciseSet) -> Unit,
    onExerciseUnitChange: (UUID, WeightUnit) -> Unit,
    onAddExercise: () -> Unit,
    onNavigateToSelectExercise: (exerciseIndex: Int) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        itemsIndexed(workoutExercises, key = { _, item -> item.id }) { index, workoutExercise ->
            SwipeToDeleteContainer(
                item = workoutExercise,
                onDismiss = onDeleteExercise
            ) {
                ExerciseInputCard(
                    exerciseDefinition = exerciseDefinitions.getOrNull(index),
                    workoutExercise = workoutExercise,
                    onAddSet = { onAddSet(workoutExercise.id) },
                    onDeleteSet = { set -> onDeleteSet(workoutExercise.id, set) },
                    onSetChanged = { setIndex, updatedSet ->
                        onSetChanged(
                            workoutExercise.id,
                            setIndex,
                            updatedSet
                        )
                    },
                    onExerciseUnitChange = { newUnit ->
                        onExerciseUnitChange(
                            workoutExercise.id,
                            newUnit
                        )
                    },
                    unit = workoutExercise.unit,
                    onNavigateToSelectExercise = { onNavigateToSelectExercise(index) }
                )
            }
        }
        item {
            Button(
                onClick = onAddExercise,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TimberOrange,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add Exercise")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun WorkoutScreenPreview() {
    TimberWorkoutLogsTheme {
        // TODO: Previewing this screen now requires a mocked ViewModel
    }
}
