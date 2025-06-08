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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    initialWorkoutName: String = "New Workout",
    onFinishWorkout: (currentWorkoutData: Workout) -> Unit,
    onOpenNotes: (currentNotes: String) -> Unit,
    onDiscardWorkout: () -> Unit,
    onOpenPlateCalculator: () -> Unit
) {
    // State is now managed locally within the composable
    var workoutName by remember { mutableStateOf(initialWorkoutName) }
    val exercises = remember { mutableStateListOf<Exercise>() }
    var workoutNotes by remember { mutableStateOf("")}
    var secondsElapsed by remember { mutableStateOf(0) }

    // LaunchedEffect to populate initial data
    LaunchedEffect(Unit) {
        if (exercises.isEmpty()) {
            // Start with a single generic exercise
            exercises.add(Exercise(name = "New Exercise 1", sets = mutableListOf(ExerciseSet())))
        }
    }

    // LaunchedEffect for the timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    // Calculate timer text from state
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

    // Stable lambdas to prevent unnecessary recompositions in the LazyColumn
    val onAddSet = remember { { exerciseId: UUID ->
        val index = exercises.indexOfFirst { it.id == exerciseId }
        if (index != -1) {
            val updatedSets = exercises[index].sets.toMutableList().apply { add(ExerciseSet()) }
            exercises[index] = exercises[index].copy(sets = updatedSets)
        }
    } }

    val onSetChanged = remember { { exerciseId: UUID, setIndex: Int, updatedSet: ExerciseSet ->
        val index = exercises.indexOfFirst { it.id == exerciseId }
        if (index != -1) {
            val updatedSets = exercises[index].sets.toMutableList()
            if (setIndex >= 0 && setIndex < updatedSets.size) {
                updatedSets[setIndex] = updatedSet
                exercises[index] = exercises[index].copy(sets = updatedSets)
            }
        }
    } }

    val onExerciseNameChange = remember { { exerciseId: UUID, newName: String ->
        val index = exercises.indexOfFirst { it.id == exerciseId }
        if(index != -1) {
            exercises[index] = exercises[index].copy(name = newName)
        }
    } }

    // FIX: Explicitly declare the type of the lambda to be () -> Unit.
    // This tells the compiler to ignore the Boolean returned by the .add() function.
    val onAddExercise: () -> Unit = remember { {
        exercises.add(Exercise(name = "New Exercise ${exercises.size + 1}", sets = mutableListOf(ExerciseSet())))
    } }

    Scaffold(
        topBar = {
            WorkoutTopAppBar(
                title = workoutName,
                timerText = timerText,
                onFinishWorkout = {
                    val finishedWorkout = Workout(
                        name = workoutName,
                        durationSeconds = secondsElapsed,
                        exercises = exercises.toMutableList(),
                        notes = workoutNotes
                    )
                    onFinishWorkout(finishedWorkout)
                }
            )
        },
        bottomBar = {
            WorkoutBottomActions(
                onOpenNotes = { onOpenNotes(workoutNotes) },
                onDiscardWorkout = onDiscardWorkout,
                onOpenPlateCalculator = onOpenPlateCalculator
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        WorkoutExerciseList(
            modifier = Modifier.padding(innerPadding),
            exercises = exercises,
            onAddSet = onAddSet,
            onSetChanged = onSetChanged,
            onExerciseNameChange = onExerciseNameChange,
            onAddExercise = onAddExercise
        )
    }
}

@Composable
private fun WorkoutExerciseList(
    modifier: Modifier = Modifier,
    exercises: List<Exercise>,
    onAddSet: (UUID) -> Unit,
    onSetChanged: (UUID, Int, ExerciseSet) -> Unit,
    onExerciseNameChange: (UUID, String) -> Unit,
    onAddExercise: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        items(items = exercises, key = { it.id }) { exercise ->
            ExerciseInputCard(
                exercise = exercise,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopAppBar(
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
fun WorkoutBottomActions(
    onOpenNotes: () -> Unit,
    onDiscardWorkout: () -> Unit,
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
        DiscardWorkoutButton(onConfirmDiscard = onDiscardWorkout)
        IconButton(onClick = onOpenPlateCalculator, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.Calculate, contentDescription = "Plate Calculator", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun DiscardWorkoutButton(
    onConfirmDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isConfirming by remember { mutableStateOf(false) }

    val clickedColor = Color(0xFFe86c6c)
    val containerColor = if (isConfirming) clickedColor else Color.Transparent
    val text = if (isConfirming) "Are you sure?" else "Discard Workout"

    TextButton(
        onClick = {
            if (isConfirming) {
                onConfirmDiscard()
            } else {
                isConfirming = true
            }
        },
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor
        ),
        modifier = modifier
    ) {
        Text(text)
    }
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
