package com.bignerdranch.android.timberworkoutlogs.ui.screen.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseDefinition
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseSet
import com.bignerdranch.android.timberworkoutlogs.models.WeightUnit
import com.bignerdranch.android.timberworkoutlogs.models.WorkoutExercise
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.components.ExerciseInputCard
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.components.WorkoutBottomActions
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.components.WorkoutTopAppBar
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberOrange
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import java.util.UUID

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSelectExercise: (exerciseIndex: Int) -> Unit,
    onOpenNotes: () -> Unit,
    onOpenPlateCalculator: () -> Unit
) {
    val workoutName by viewModel.workoutName.collectAsStateWithLifecycle()
    val workoutExercises = viewModel.workoutExercises
    val exerciseDefinitions = viewModel.exerciseDefinitions
    val timerText by viewModel.timerText.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        WorkoutTopAppBar(
            title = workoutName,
            timerText = timerText,
            onFinishWorkout = { viewModel.onFinishWorkout(onNavigateBack) }
        )

        WorkoutExerciseList(
            modifier = Modifier.weight(1f),
            workoutExercises = workoutExercises,
            exerciseDefinitions = exerciseDefinitions,
            onAddSet = viewModel::onAddSet,
            onSetChanged = viewModel::onSetChanged,
            onExerciseUnitChange = viewModel::onExerciseUnitChange,
            onAddExercise = viewModel::onAddExercise,
            onNavigateToSelectExercise = onNavigateToSelectExercise
        )

        WorkoutBottomActions(
            onOpenNotes = onOpenNotes,
            onDiscardWorkout = { viewModel.onDiscardWorkout(onNavigateBack) },
            onOpenPlateCalculator = onOpenPlateCalculator
        )
    }
}

@Composable
private fun WorkoutExerciseList(
    modifier: Modifier = Modifier,
    workoutExercises: List<WorkoutExercise>,
    exerciseDefinitions: List<ExerciseDefinition?>,
    onAddSet: (UUID) -> Unit,
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
        itemsIndexed(workoutExercises) { index, workoutExercise ->
            ExerciseInputCard(
                exerciseDefinition = exerciseDefinitions.getOrNull(index),
                workoutExercise = workoutExercise,
                onAddSet = { onAddSet(workoutExercise.id) },
                onSetChanged = { setIndex, updatedSet -> onSetChanged(workoutExercise.id, setIndex, updatedSet) },
                onExerciseUnitChange = { newUnit -> onExerciseUnitChange(workoutExercise.id, newUnit) },
                onNavigateToSelectExercise = { onNavigateToSelectExercise(index) }
            )
        }
        item {
            Button(
                onClick = onAddExercise,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TimberOrange,
                    contentColor = Color.Black
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
