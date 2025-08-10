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
import androidx.compose.runtime.Immutable
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
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExerciseCount
import com.android.timberworkoutlogs.ui.common.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.screen.exercise.components.ExerciseInputCard
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutBottomActions
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutTopAppBar
import com.android.timberworkoutlogs.ui.theme.TimberOrange
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import kotlinx.coroutines.delay
import java.util.UUID

private const val TIMER_TAG = "Timer Service"

@Immutable
data class WorkoutScreenState(
    val workoutExercises: List<WorkoutExercise>,
    val exerciseDefinitions: List<ExerciseDefinition?>,
    val isWorkoutEmpty: Boolean,
    val templates: List<WorkoutTemplateWithExerciseCount>,
    val timerText: String
)

@Immutable
data class WorkoutScreenActions(
    val onNavigateBack: () -> Unit,
    val onNavigateToSelectExercise: (exerciseIndex: Int) -> Unit,
    val onOpenNotes: () -> Unit,
    val onOpenPlateCalculator: () -> Unit,
    val onAddSet: (UUID) -> Unit,
    val deleteExercise: (WorkoutExercise) -> Unit,
    val deleteSet: (UUID, ExerciseSet) -> Unit,
    val onSetChanged: (UUID, Int, ExerciseSet) -> Unit,
    val onExerciseUnitChange: (UUID, WeightUnit) -> Unit,
    val onAddExercise: () -> Unit,
    val onDiscardWorkout: () -> Unit,
    val onFinishWorkout: () -> Unit,
    val onImportFromTemplate: (Long) -> Unit,
    val onStartTimer: () -> Unit,
    val stopTimer: () -> Unit
)

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun WorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSelectExercise: (exerciseIndex: Int) -> Unit,
    onOpenNotes: () -> Unit,
    onOpenPlateCalculator: () -> Unit
) {
    val state = WorkoutScreenState(
        workoutExercises = workoutViewModel.workoutExercises,
        exerciseDefinitions = workoutViewModel.exerciseDefinitions,
        isWorkoutEmpty = workoutViewModel.isWorkoutEmpty.collectAsStateWithLifecycle(initialValue = true).value,
        templates = workoutViewModel.templates.collectAsStateWithLifecycle().value,
        timerText = workoutViewModel.timerText.collectAsState().value
    )

    val actions = WorkoutScreenActions(
        onNavigateBack = onNavigateBack,
        onNavigateToSelectExercise = onNavigateToSelectExercise,
        onOpenNotes = onOpenNotes,
        onOpenPlateCalculator = onOpenPlateCalculator,
        onAddSet = workoutViewModel::onAddSet,
        deleteExercise = workoutViewModel::deleteExercise,
        deleteSet = workoutViewModel::deleteSet,
        onSetChanged = workoutViewModel::onSetChanged,
        onExerciseUnitChange = workoutViewModel::onExerciseUnitChange,
        onAddExercise = workoutViewModel::onAddExercise,
        onDiscardWorkout = { workoutViewModel.onDiscardWorkout(onNavigateBack) },
        onFinishWorkout = { workoutViewModel.onFinishWorkout(onNavigateBack) },
        onImportFromTemplate = workoutViewModel::importExercisesFromTemplate,
        onStartTimer = workoutViewModel::startTimer,
        stopTimer = { workoutViewModel.timerService?.stopTimer() }
    )

    WorkoutScreenContent(state = state, actions = actions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutScreenContent(
    state: WorkoutScreenState,
    actions: WorkoutScreenActions
) {
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
                actions.onStartTimer()
            }
        }
    )
    var showTemplateSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var isConfirmingFinish by remember { mutableStateOf(false) }
    var isConfirmingDiscard by remember { mutableStateOf(false) }

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

    LaunchedEffect(isConfirmingDiscard) {
        if (isConfirmingDiscard) {
            Toast.makeText(context, "Tap once more to confirm discard", Toast.LENGTH_SHORT).show()
            delay(3000)
            if (isConfirmingDiscard) {
                isConfirmingDiscard = false
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotificationPermission) {
                actions.onStartTimer()
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            actions.onStartTimer()
        }
    }

    BackHandler(enabled = true) {
        if (state.isWorkoutEmpty) {
            Log.d(TIMER_TAG, "Timer service being stopped through back handler.")
            actions.stopTimer()
            actions.onDiscardWorkout()
        } else {
            actions.onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            WorkoutTopAppBar(
                title = "Log Workout",
                timerText = state.timerText,
                onDiscardWorkout = actions.onDiscardWorkout,
                onImportFromTemplate = { showTemplateSheet = true },
                isConfirmingDiscard = isConfirmingDiscard,
                onConfirmDiscard = { isConfirmingDiscard = true }
            )
        },
        bottomBar = {
            WorkoutBottomActions(
                onOpenNotes = actions.onOpenNotes,
                onFinishWorkout = {
                    Log.d(TIMER_TAG, "Timer service being stopped as workout is finished.")
                    actions.onFinishWorkout()
                },
                isFinishEnabled = !state.isWorkoutEmpty,
                isConfirmingFinish = isConfirmingFinish,
                onConfirmFinish = { isConfirmingFinish = true },
                onOpenPlateCalculator = actions.onOpenPlateCalculator
            )
        }
    ) { innerPadding ->
        WorkoutExerciseList(
            modifier = Modifier.padding(innerPadding),
            workoutExercises = state.workoutExercises,
            exerciseDefinitions = state.exerciseDefinitions,
            onAddSet = actions.onAddSet,
            onDeleteExercise = actions.deleteExercise,
            onDeleteSet = actions.deleteSet,
            onSetChanged = actions.onSetChanged,
            onExerciseUnitChange = actions.onExerciseUnitChange,
            onAddExercise = actions.onAddExercise,
            onNavigateToSelectExercise = actions.onNavigateToSelectExercise
        )
    }

    if (showTemplateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplateSheet = false },
            sheetState = sheetState
        ) {
            LazyColumn {
                items(state.templates) { template ->
                    Text(
                        text = template.workoutTemplate.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                actions.onImportFromTemplate(template.workoutTemplate.id)
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
        val exerciseDef1 = ExerciseDefinition(
            name = "Barbell Bench Press",
            logType = LogType.WEIGHT_AND_REPS,
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = listOf(
                MuscleGroup.CHEST
            )
        )
        val exerciseDef2 = ExerciseDefinition(
            name = "Cable Crossovers",
            logType = LogType.WEIGHT_AND_REPS,
            equipment = ExerciseEquipment.CABLE,
            muscleGroups = listOf(
                MuscleGroup.CHEST
            )
        )
        val state = WorkoutScreenState(
            workoutExercises = listOf(
                WorkoutExercise(
                    workoutId = 0L,
                    definitionId = exerciseDef1.id,
                    sets = listOf(
                        WeightAndRepsSet(reps = 10, weight = 135.0, isDone = true),
                        WeightAndRepsSet(reps = 8, weight = 145.0, isDone = true),
                        WeightAndRepsSet(reps = 6, weight = 155.0, isDone = false)
                    )
                ),
                WorkoutExercise(
                    workoutId = 0L,
                    definitionId = exerciseDef2.id,
                    sets = listOf(
                        WeightAndRepsSet(reps = 12, weight = 45.0, isDone = true),
                        WeightAndRepsSet(reps = 10, weight = 50.0, isDone = false)
                    )
                )
            ),
            exerciseDefinitions = listOf(exerciseDef1, exerciseDef2),
            isWorkoutEmpty = false,
            templates = listOf(
                WorkoutTemplateWithExerciseCount(WorkoutTemplate(id = 1, name = "Chest Day"), 5),
                WorkoutTemplateWithExerciseCount(WorkoutTemplate(id = 2, name = "Back Day"), 6)
            ),
            timerText = "00:15:23"
        )

        val actions = WorkoutScreenActions(
            onNavigateBack = {},
            onNavigateToSelectExercise = {},
            onOpenNotes = {},
            onOpenPlateCalculator = {},
            onAddSet = {},
            deleteExercise = {},
            deleteSet = { _, _ -> },
            onSetChanged = { _, _, _ -> },
            onExerciseUnitChange = { _, _ -> },
            onAddExercise = {},
            onDiscardWorkout = {},
            onFinishWorkout = {},
            onImportFromTemplate = {},
            onStartTimer = {},
            stopTimer = {}
        )

        WorkoutScreenContent(state = state, actions = actions)
    }
}
