package com.bignerdranch.android.timberworkoutlogs.ui.screen.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.models.WorkoutExercise
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseSet
import com.bignerdranch.android.timberworkoutlogs.models.Workout
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.components.ExerciseInputCard
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.components.WorkoutBottomActions
import com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.components.WorkoutTopAppBar
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberOrange
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.UUID

@Composable
fun WorkoutScreen(
    initialWorkoutName: String = "New Workout",
    onFinishWorkout: (currentWorkoutData: Workout) -> Unit,
    onOpenNotes: (currentNotes: String) -> Unit,
    onDiscardWorkout: () -> Unit,
    onOpenPlateCalculator: () -> Unit
) {
    var workoutName by remember { mutableStateOf(initialWorkoutName) }
    val workoutExercises = remember { mutableStateListOf<WorkoutExercise>() }
    var workoutNotes by remember { mutableStateOf("")}
    var secondsElapsed by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (workoutExercises.isEmpty()) {
            workoutExercises.add(WorkoutExercise(sets = mutableListOf(ExerciseSet())))
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    val timerText = remember(secondsElapsed) {
        val hours = secondsElapsed / 3600
        val minutes = (secondsElapsed % 3600) / 60
        val secs = secondsElapsed % 60
        if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        }
    }

    val onAddSet = remember { { exerciseId: UUID ->
        val index = workoutExercises.indexOfFirst { it.id == exerciseId }
        if (index != -1) {
            val updatedSets = workoutExercises[index].sets.toMutableList().apply { add(ExerciseSet()) }
            workoutExercises[index] = workoutExercises[index].copy(sets = updatedSets)
        }
    } }

    val onSetChanged = remember { { exerciseId: UUID, setIndex: Int, updatedSet: ExerciseSet ->
        val index = workoutExercises.indexOfFirst { it.id == exerciseId }
        if (index != -1) {
            val updatedSets = workoutExercises[index].sets.toMutableList()
            if (setIndex >= 0 && setIndex < updatedSets.size) {
                updatedSets[setIndex] = updatedSet
                workoutExercises[index] = workoutExercises[index].copy(sets = updatedSets)
            }
        }
    } }

    val onExerciseNameChange = remember { { exerciseId: UUID, newName: String ->
        val index = workoutExercises.indexOfFirst { it.id == exerciseId }
        if(index != -1) {
            workoutExercises[index] = workoutExercises[index].copy(name = newName)
        }
    } }

    val onAddWorkoutExercise: () -> Unit = remember { {
        workoutExercises.add(WorkoutExercise(sets = mutableListOf(ExerciseSet())))
    } }

    // FIX: Replaced the nested Scaffold with a Column to prevent the double app bar bug.
    // This Column now manually arranges the screen's components.
    Column(modifier = Modifier.fillMaxSize()) {
        WorkoutTopAppBar(
            title = workoutName,
            timerText = timerText,
            onFinishWorkout = {
                val finishedWorkout = Workout(
                    name = workoutName,
                    durationSeconds = secondsElapsed,
                    workoutExercises = workoutExercises.toMutableList(),
                    notes = workoutNotes
                )
                onFinishWorkout(finishedWorkout)
            }
        )

        // The exercise list now takes up the remaining available space.
        WorkoutExerciseList(
            modifier = Modifier.weight(1f), // Use weight to fill the space
            workoutExercises = workoutExercises,
            onAddSet = onAddSet,
            onSetChanged = onSetChanged,
            onExerciseNameChange = onExerciseNameChange,
            onAddExercise = onAddWorkoutExercise
        )

        // The bottom actions are placed at the end of the column.
        WorkoutBottomActions(
            onOpenNotes = { onOpenNotes(workoutNotes) },
            onDiscardWorkout = onDiscardWorkout,
            onOpenPlateCalculator = onOpenPlateCalculator
        )
    }
}

@Composable
private fun WorkoutExerciseList(
    modifier: Modifier = Modifier,
    workoutExercises: List<WorkoutExercise>,
    onAddSet: (UUID) -> Unit,
    onSetChanged: (UUID, Int, ExerciseSet) -> Unit,
    onExerciseNameChange: (UUID, String) -> Unit,
    onAddExercise: () -> Unit
) {
    LazyColumn(
        // FIX: The modifier from the parent now correctly controls the space.
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        items(items = workoutExercises, key = { it.id }) { exercise ->
            ExerciseInputCard(
                workoutExercise = exercise,
                onAddSet = { onAddSet(exercise.id) },
                onSetChanged = { setIndex, updatedSet -> onSetChanged(exercise.id, setIndex, updatedSet) },
                onExerciseNameChange = { newName -> onExerciseNameChange(exercise.id, newName) }
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
        WorkoutScreen(
            onFinishWorkout = {},
            onOpenNotes = {},
            onDiscardWorkout = {},
            onOpenPlateCalculator = {}
        )
    }
}