//@file:OptIn(ExperimentalMaterial3Api::class)
//
//package com.bignerdranch.android.timberworkoutlogs.ui.screen
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.ui.res.painterResource
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.bignerdranch.android.timberworkoutlogs.R
//import com.bignerdranch.android.timberworkoutlogs.models.Exercise
//import com.bignerdranch.android.timberworkoutlogs.models.ExerciseSet
//import com.bignerdranch.android.timberworkoutlogs.models.Workout
//import kotlinx.coroutines.delay
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun WorkoutScreen(
//    workout: Workout,
//    workoutName: String = getDefaultWorkoutName(),
//    onFinishWorkout: () -> Unit = {},
//    onDiscardWorkout: () -> Unit = {},
//    onNotesClick: () -> Unit = {},
//    onPlateCalculatorClick: () -> Unit = {},
//    onAddSet: (exerciseId: Long, weight: Double, reps: Int) -> Unit = { _, _, _ -> }
//) {
//    var timerSeconds by remember { mutableIntStateOf(0) }
//
//    // Timer effect
//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(1000)
//            timerSeconds++
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Header Row
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.Top
//        ) {
//            // Workout Name
//            Text(
//                text = workoutName,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.weight(1f)
//            )
//
//            // Timer
//            Text(
//                text = formatTime(timerSeconds),
//                fontSize = 16.sp,
//                modifier = Modifier.padding(horizontal = 16.dp),
//                textAlign = TextAlign.Center
//            )
//
//            // Finish Workout Button
//            IconButton(
//                onClick = onFinishWorkout
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_checkered_flag),
//                    contentDescription = "Finish Workout"
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Exercises List
//        LazyColumn(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(workout.exercises) { exercise ->
//                ExerciseCard(
//                    exercise = exercise,
//                    onAddSet = { weight, reps ->
//                        onAddSet(exercise.id, weight, reps)
//                    }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Bottom Row
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Notes Button
//            IconButton(onClick = onNotesClick) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_notes),
//                    contentDescription = "Workout Notes"
//                )
//            }
//
//            // Discard Workout Button
//            TextButton(
//                onClick = onDiscardWorkout,
//                modifier = Modifier.weight(1f)
//            ) {
//                Text("Discard Workout")
//            }
//
//            // Plate Calculator Button
//            IconButton(onClick = onPlateCalculatorClick) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_calculator),
//                    contentDescription = "Plate Calculator"
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun ExerciseCard(
//    exercise: Exercise,
//    onAddSet: (weight: Double, reps: Int) -> Unit = { _, _ -> }
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = exercise.name,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Sets
//            if (exercise.sets.isNotEmpty()) {
//                exercise.sets.forEachIndexed { index, set ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 4.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text("Set ${index + 1}")
//                        Text(
//                            text = if (set.weight == 0.0) {
//                                "Bodyweight × ${set.reps}"
//                            } else {
//                                "${formatWeight(set.weight)} × ${set.reps}"
//                            }
//                        )
//                    }
//                }
//            } else {
//                Text(
//                    text = "No sets recorded",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Add Set Button
//            TextButton(
//                onClick = {
//                    // For now, add a default set - you can make this more interactive
//                    onAddSet(0.0, 1)
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = null,
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Add Set")
//            }
//        }
//    }
//}
//
//// Helper function to get default workout name with current date
//fun getDefaultWorkoutName(): String {
//    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//    val currentDate = dateFormat.format(Date())
//    return "Workout - $currentDate"
//}
//
//// Helper function to format weight
//fun formatWeight(weight: Double): String {
//    return if (weight == weight.toInt().toDouble()) {
//        "${weight.toInt()} kg"
//    } else {
//        "$weight kg"
//    }
//}
//// Helper function to format timer
//fun formatTime(seconds: Int): String {
//    val hours = seconds / 3600
//    val minutes = (seconds % 3600) / 60
//    val secs = seconds % 60
//    return String.format(locale = Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
//}
//
//@Preview(showBackground = true)
//@Composable
//fun WorkoutScreenPreview() {
//    MaterialTheme {
//        val sampleWorkout = Workout(
//            id = 1,
//            duration = 45,
//            exercises = mutableListOf(
//                Exercise(
//                    id = 1,
//                    name = "Bench Press",
//                    sets = mutableListOf(
//                        ExerciseSet(1, 135.0, 8),
//                        ExerciseSet(2, 145.0, 6),
//                        ExerciseSet(3, 155.0, 4)
//                    )
//                ),
//                Exercise(
//                    id = 2,
//                    name = "Incline Dumbbell Press",
//                    sets = mutableListOf(
//                        ExerciseSet(4, 60.0, 10),
//                        ExerciseSet(5, 65.0, 8)
//                    )
//                ),
//                Exercise(
//                    id = 3,
//                    name = "Push-ups",
//                    sets = mutableListOf(
//                        ExerciseSet(6, 0.0, 15),
//                        ExerciseSet(7, 0.0, 12),
//                        ExerciseSet(8, 0.0, 10)
//                    )
//                )
//            )
//        )
//
//        WorkoutScreen(
//            workout = sampleWorkout,
//            workoutName = "Push Day" // Override default for preview
//        )
//    }
//}