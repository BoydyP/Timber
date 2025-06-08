package com.bignerdranch.android.timberworkoutlogs.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.models.Exercise
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseSet
import com.bignerdranch.android.timberworkoutlogs.models.Workout
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberOrange
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme

import kotlinx.coroutines.delay
import java.util.Locale

// Note: The old data classes WorkoutSet and ExerciseEntry are removed from this file.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewWorkoutScreen(
    // workout: Workout, // You might pass a full Workout object in the future
    initialWorkoutName: String = "New Workout", // Can be dynamic
    onFinishWorkout: (currentWorkoutData: Workout) -> Unit, // Pass back the workout data
    onOpenNotes: (currentNotes: String) -> Unit, // Pass current notes
    onUpdateNotes: (newNotes: String) -> Unit, // To update notes from a potential notes screen
    onOpenPlateCalculator: () -> Unit
) {
    // Local state for the current workout being built
    var workoutName by remember { mutableStateOf(initialWorkoutName) }
    val exercises = remember { mutableStateListOf<Exercise>() }
    var workoutNotes by remember { mutableStateOf("")} // For session notes

    // Initialize with placeholder exercises if the list is empty (e.g., on first composition)
    LaunchedEffect(Unit) {
        if (exercises.isEmpty()) {
            exercises.addAll(
                listOf(
                    // These now get unique IDs automatically
                    Exercise(name = "Bench Press (Barbell)", sets = mutableListOf(ExerciseSet(weight = 100.0, reps = 8), ExerciseSet(weight = 95.0, reps = 7))),
                )
            )
        }
    }

    var secondsElapsed by remember { mutableStateOf(0) }
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

    Scaffold(
        topBar = {
            NewWorkoutTopAppBar(
                title = workoutName, // Use dynamic workout name
                timerText = timerText,
                onFinishWorkout = {
                    // Construct the Workout object to pass back
                    val finishedWorkout = Workout(
                        // id will likely be generated when saving
                        name = workoutName,
                        durationSeconds = secondsElapsed,
                        exercises = exercises.toMutableList(), // Create a new list instance
                        notes = workoutNotes
                    )
                    onFinishWorkout(finishedWorkout)
                }
            )
        },
        bottomBar = {
            NewWorkoutBottomActions(
                onOpenNotes = { onOpenNotes(workoutNotes) },
                onOpenPlateCalculator = onOpenPlateCalculator
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(exercises, key = { it.id }) { exercise -> // The key is now guaranteed to be unique
                ExerciseInputCard(
                    exercise = exercise,
                    onAddSet = {
                        // Create a new ExerciseSet instance
                        exercise.sets.add(ExerciseSet())
                        // Force recomposition by creating a new list reference if needed,
                        // though SnapshotStateList should handle this.
                    },
                    onSetChanged = { setIndex, updatedSet ->
                        if (setIndex >= 0 && setIndex < exercise.sets.size) {
                            exercise.sets[setIndex] = updatedSet
                        }
                    },
                    onExerciseNameChange = { newName ->
                        val exerciseIndex = exercises.indexOfFirst { it.id == exercise.id }
                        if(exerciseIndex != -1) {
                            // Creating a copy is a good practice for state updates
                            exercises[exerciseIndex] = exercises[exerciseIndex].copy(name = newName)
                        }
                    }
                )
            }
            item {
                Button(
                    onClick = {
                        exercises.add(Exercise(name = "New Exercise ${exercises.size + 1}", sets = mutableListOf(ExerciseSet())))
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewWorkoutTopAppBar(
    title: String,
    timerText: String,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        actions = {
            Text(
                text = timerText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp)
            )
            IconButton(onClick = onFinishWorkout) {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = "Finish Workout"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier
    )
}

@Composable
fun ExerciseInputCard(
    exercise: Exercise,
    onAddSet: () -> Unit,
    onSetChanged: (setIndex: Int, updatedSet: ExerciseSet) -> Unit,
    onExerciseNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = exercise.name,
                onValueChange = onExerciseNameChange,
                label = { Text("Exercise Name") },
                textStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                singleLine = true
            )

            exercise.sets.forEachIndexed { index, set ->
                SetInputRow(
                    setNumber = index + 1,
                    workoutSet = set,
                    onWeightChange = { newWeightStr ->
                        val newWeight = newWeightStr.toDoubleOrNull() ?: set.weight
                        onSetChanged(index, set.copy(weight = newWeight))
                    },
                    onRepsChange = { newRepsStr ->
                        val newReps = newRepsStr.toIntOrNull() ?: set.reps
                        onSetChanged(index, set.copy(reps = newReps))
                    },
                    onDoneChange = { isDone ->
                        onSetChanged(index, set.copy(isDone = isDone))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            IconButton(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Add Set",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SetInputRow(
    setNumber: Int,
    workoutSet: ExerciseSet,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onDoneChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = if (workoutSet.weight == 0.0 && workoutSet.reps == 0) "" else workoutSet.weight.toString(),
            onValueChange = onWeightChange,
            label = { Text("Weight") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text("kg/lbs") }
        )
        OutlinedTextField(
            value = if (workoutSet.weight == 0.0 && workoutSet.reps == 0) "" else workoutSet.reps.toString(),
            onValueChange = onRepsChange,
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Checkbox(
            checked = workoutSet.isDone,
            onCheckedChange = onDoneChange,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun NewWorkoutBottomActions(
    onOpenNotes: () -> Unit,
    onOpenPlateCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenNotes, modifier = Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Session Notes", modifier = Modifier.size(32.dp))
        }
        TextButton(onClick = { /* TODO: Handle discard workout action */ }) {
            Text("Discard Workout")
        }
        IconButton(onClick = onOpenPlateCalculator, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.Calculate, contentDescription = "Plate Calculator", modifier = Modifier.size(32.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun NewWorkoutScreenPreview() {
    TimberWorkoutLogsTheme {
        NewWorkoutScreen(
            onFinishWorkout = {},
            onOpenNotes = {},
            onUpdateNotes = {},
            onOpenPlateCalculator = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExerciseInputCardPreview() {
    TimberWorkoutLogsTheme {
        var exercise by remember {
            mutableStateOf(
                Exercise(name = "Bench Press", sets = mutableListOf(ExerciseSet(weight = 100.0, reps = 5, isDone = true), ExerciseSet(weight = 120.0, reps = 3)))
            )
        }
        ExerciseInputCard(
            exercise = exercise,
            onAddSet = { exercise.sets.add(ExerciseSet()) },
            onSetChanged = { index, updatedSet -> exercise.sets[index] = updatedSet },
            onExerciseNameChange = { newName -> exercise = exercise.copy(name = newName) }
        )
    }
}
