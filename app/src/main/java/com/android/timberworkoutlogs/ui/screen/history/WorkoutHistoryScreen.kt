package com.android.timberworkoutlogs.ui.screen.history

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
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
import com.android.timberworkoutlogs.database.ExerciseDefinitionWithCount
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutDao
import com.android.timberworkoutlogs.database.WorkoutExerciseDao
import com.android.timberworkoutlogs.database.WorkoutExerciseWithDate
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.database.WorkoutWithExercises
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.ui.common.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutHistoryViewModel,
    onNavigateToWorkout: (workoutId: Long) -> Unit,
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
                items(
                    items = workoutDisplayItems,
                    key = { it.workout.id }
                ) { displayItem ->
                    SwipeToDeleteContainer(
                        item = displayItem,
                        onDismiss = { viewModel.deleteWorkout(it) }
                    ) {
                        WorkoutHistoryItemCard(
                            displayItem = displayItem,
                            modifier = Modifier.clickable {
                                onNavigateToWorkout(displayItem.workout.id)
                            }
                        )
                    }
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
        // Single source of truth for the fake, so lookups by id agree with the list.
        private val sampleWorkouts = listOf(
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

        override fun getAllWorkouts(): Flow<List<Workout>> = flowOf(sampleWorkouts)
        override suspend fun insertWorkout(workout: Workout): Long = 0L
        override suspend fun updateWorkout(workout: Workout) {}
        override suspend fun deleteWorkout(workout: Workout) {}
        override suspend fun deleteAllWorkouts() {}
        override suspend fun getWorkoutCount(): Int = sampleWorkouts.size

        // Like Room, an id with no matching row yields nothing rather than a fabricated workout.
        override fun getWorkoutFlow(id: Long): Flow<Workout> =
            sampleWorkouts.find { it.id == id }?.let { flowOf(it) } ?: emptyFlow()

        override suspend fun getWorkout(id: Long): Workout? = sampleWorkouts.find { it.id == id }
        override fun getWorkoutsWithExercisesFrom(startTimeMillis: Long): Flow<List<WorkoutWithExercises>> =
            flowOf(emptyList())
        
        override fun getExerciseDefinitionsWithWorkoutHistory(): Flow<List<ExerciseDefinition>> =
            flowOf(emptyList())
        
        override fun getExerciseHistoryData(definitionId: UUID, fromTime: Long): Flow<List<WorkoutExerciseWithDate>> =
            flowOf(emptyList())
        
        override fun getExerciseDefinitionsWithWorkoutCounts(): Flow<List<ExerciseDefinitionWithCount>> =
            flowOf(emptyList())

        override fun getPersonalRecordsMaxLifts(exerciseNames: List<String>): Flow<List<com.android.timberworkoutlogs.database.MaxLiftData>> =
            flowOf(emptyList())

        override suspend fun getMostRecentWorkoutExercise(definitionId: UUID): WorkoutExercise? = null
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
    val fakeDataStore = object : DataStore<Preferences> {
        private val state = MutableStateFlow(emptyPreferences())
        override val data: Flow<Preferences> = state
        override suspend fun updateData(
            transform: suspend (Preferences) -> Preferences
        ): Preferences = transform(state.value).also { state.value = it }
    }
    val fakeSettingsRepository = SettingsRepository(fakeDataStore)

    // 3. Create a real ViewModel with the fake repository.
    // Note: Creating a ViewModel directly in a Composable is generally an antipattern,
    // but it is a standard and necessary practice for creating isolated @Previews.
    val previewViewModel = WorkoutHistoryViewModel(fakeWorkoutRepository, fakeSettingsRepository)

    TimberWorkoutLogsTheme {
        WorkoutHistoryScreen(
            viewModel = previewViewModel,
            onNavigateToWorkout = {}
        )
    }
}
