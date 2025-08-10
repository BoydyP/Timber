package com.android.timberworkoutlogs.ui.screen.workout

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.android.timberworkoutlogs.ui.common.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.screen.exercise.components.ExerciseInputCard
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutBottomActions
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutTopAppBar
import com.android.timberworkoutlogs.ui.theme.TimberOrange
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import kotlinx.coroutines.delay
import java.util.UUID

private const val TIMER_TAG = "Timer Service"

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
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
    val templates by workoutViewModel.templates.collectAsStateWithLifecycle()
    val timerText by workoutViewModel.timerText.collectAsState()
    val context = LocalContext.current
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
            if (isGranted) {
                workoutViewModel.startTimer()
            }
        }
    )
    var showTemplateSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var isConfirmingFinish by remember { mutableStateOf(false) }

    LaunchedEffect(isConfirmingFinish) {
        if (isConfirmingFinish) {
            Toast.makeText(context, "Tap once more to confirm completion", Toast.LENGTH_SHORT)
                .show()
            delay(3000)
            if (isConfirmingFinish) {
                isConfirmingFinish = false
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotificationPermission) {
                workoutViewModel.startTimer()
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            workoutViewModel.startTimer()
        }
    }

    BackHandler(enabled = true) {
        if (isWorkoutEmpty) {
            Log.d(TIMER_TAG, "Timer service being stopped through back handler.")
            workoutViewModel.timerService?.stopTimer()
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
                    workoutViewModel.onDiscardWorkout(onNavigateBack)
                },
                onImportFromTemplate = { showTemplateSheet = true }
            )
        },
        bottomBar = {
            WorkoutBottomActions(
                onOpenNotes = onOpenNotes,
                onFinishWorkout = {
                    Log.d(TIMER_TAG, "Timer service being stopped as workout is finished.")
                    workoutViewModel.onFinishWorkout(onNavigateBack)
                },
                isFinishEnabled = !isWorkoutEmpty,
                isConfirmingFinish = isConfirmingFinish,
                onConfirmFinish = { isConfirmingFinish = true },
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

    if (showTemplateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplateSheet = false },
            sheetState = sheetState
        ) {
            LazyColumn {
                items(templates) { template ->
                    Text(
                        text = template.workoutTemplate.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                workoutViewModel.importExercisesFromTemplate(template.workoutTemplate.id)
                                showTemplateSheet = false
                            }
                            .padding(16.dp)
                    )
                }
            }
        }
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
