package com.android.timberworkoutlogs.ui.screen.workout

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.database.WorkoutExerciseDao
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutHistoryItemCard
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutHistoryViewModel,
    onNavigateToWorkout: () -> Unit, // TODO: This might be used to navigate to a workout details screen
    modifier: Modifier = Modifier
) {
    val workoutDisplayItems by viewModel.allWorkoutDisplayItems.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Workout History",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (workoutDisplayItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No workouts have been recorded yet.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(workoutDisplayItems) { displayItem ->
                    WorkoutHistoryItemCard(displayItem = displayItem)
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun WorkoutHistoryScreenPreview() {
    // This preview demonstrates how to mock dependencies for a screen.
    // 1. Create Fake DAOs that implement all methods from the real interfaces.
    class FakeWorkoutDao : WorkoutDao {
        override fun getAllWorkouts(): Flow<List<Workout>> {
            val sampleWorkouts = listOf(
                Workout(
                    id = 1,
                    name = "Chest Day",
                    startTime = System.currentTimeMillis() - 86400000,
                    durationSeconds = 3600
                ),
                Workout(
                    id = 2,
                    name = "Leg Day",
                    startTime = System.currentTimeMillis() - 172800000,
                    durationSeconds = 4500
                )
            )
            return flowOf(sampleWorkouts)
        }
        override suspend fun insertWorkout(workout: Workout): Long = 0L
        override suspend fun updateWorkout(workout: Workout) {}
        override suspend fun deleteWorkout(workout: Workout) {}
        override suspend fun getWorkoutCount(): Int = 2
        override fun getWorkoutFlow(id: Long): Flow<Workout> = flowOf(Workout(id = id))
        override suspend fun getWorkout(id: Long): Workout? = Workout(id = id)
    }

    class FakeWorkoutExerciseDao : WorkoutExerciseDao {
        override suspend fun insertWorkoutExercises(exercises: List<WorkoutExercise>) {}
        override suspend fun getExercisesForWorkout(workoutId: Long): List<WorkoutExercise> {
            return if (workoutId == 1L) {
                listOf(
                    WorkoutExercise(
                        workoutId = 1,
                        definitionId = UUID.randomUUID(),
                        unit = WeightUnit.KG,
                        sets = mutableListOf<ExerciseSet>(
                            WeightAndRepsSet(80.0, 10),
                            WeightAndRepsSet(85.0, 8)
                        )
                    ),
                    WorkoutExercise(
                        workoutId = 1,
                        definitionId = UUID.randomUUID(),
                        unit = WeightUnit.KG,
                        sets = mutableListOf<ExerciseSet>(WeightAndRepsSet(20.0, 12))
                    )
                )
            } else {
                listOf(
                    WorkoutExercise(
                        workoutId = 2,
                        definitionId = UUID.randomUUID(),
                        unit = WeightUnit.LB,
                        sets = mutableListOf<ExerciseSet>(
                            WeightAndRepsSet(225.0, 5),
                            WeightAndRepsSet(245.0, 3)
                        )
                    )
                )
            }
        }
    }

    // 2. Create a Fake Repository using the Fakes.
    val fakeWorkoutRepository = WorkoutRepository(FakeWorkoutDao(), FakeWorkoutExerciseDao())

    // 3. Create a real ViewModel with the fake repository.
    // Note: Creating a ViewModel directly in a Composable is generally an anti-pattern,
    // but it is a standard and necessary practice for creating isolated @Previews.
    val previewViewModel = WorkoutHistoryViewModel(fakeWorkoutRepository)

    TimberWorkoutLogsTheme {
        WorkoutHistoryScreen(
            viewModel = previewViewModel,
            onNavigateToWorkout = {}
        )
    }
}
