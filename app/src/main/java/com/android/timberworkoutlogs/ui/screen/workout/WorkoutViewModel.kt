package com.android.timberworkoutlogs.ui.screen.workout

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

private const val TAG = "WorkoutViewModel"

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository
) : ViewModel() {

    val workoutExercises = mutableStateListOf<WorkoutExercise>()
    val exerciseDefinitions = mutableStateListOf<ExerciseDefinition?>()

    val isWorkoutEmpty: StateFlow<Boolean> = snapshotFlow {
        val hasValidExerciseWithData = workoutExercises.indices.any { index ->
            val definition = exerciseDefinitions.getOrNull(index)
            val exercise = workoutExercises.getOrNull(index)
            if (definition == null || exercise == null || exercise.sets.isEmpty()) {
                false
            } else {
                exercise.sets.any { set ->
                    when (set) {
                        is WeightAndRepsSet -> set.weight > 0.0 || set.reps > 0
                        is RepsOnlySet -> set.reps > 0
                        is TimedSet -> set.durationSeconds > 0
                        is DistanceAndTimeSet -> set.distance > 0.0 || set.durationSeconds > 0
                    }
                }
            }
        }
        !hasValidExerciseWithData
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    private val _timerText = MutableStateFlow("00:00")
    val timerText = _timerText.asStateFlow()

    private var currentWorkoutId: Long? = null
    private var secondsElapsed = 0
    private var timerJob: Job? = null

    init {
        Log.d(TAG, "ViewModel initialized")
        startNewWorkoutSession()
        startTimer()
    }

    private fun startNewWorkoutSession() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val newWorkout = Workout(startTime = currentTime)
            val id = workoutRepository.insertWorkout(newWorkout)
            currentWorkoutId = id
            Log.d(TAG, "New workout session started with ID: $id")
            onAddExercise()
        }
    }

    private fun startTimer() {
        Log.d(TAG, "Starting timer...")
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                secondsElapsed++
                _timerText.value = formatTime(secondsElapsed)
            }
        }
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
        else String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }

    fun onExerciseSelected(exerciseIndex: Int, definitionId: UUID) {
        viewModelScope.launch {
            val definition = exerciseDefinitionRepository.getExerciseDefinition(definitionId)
            if (exerciseIndex >= 0 && exerciseIndex < workoutExercises.size) {
                val initialSet = createDefaultSetForLogType(definition.logType)

                exerciseDefinitions[exerciseIndex] = definition
                workoutExercises[exerciseIndex] = workoutExercises[exerciseIndex].copy(
                    definitionId = definitionId,
                    sets = listOf(initialSet)
                )
            }
        }
    }

    fun onAddExercise() {
        currentWorkoutId?.let { id ->
            val placeholderDefinitionId = UUID.randomUUID()
            workoutExercises.add(
                WorkoutExercise(
                    workoutId = id,
                    definitionId = placeholderDefinitionId,
                    sets = listOf()
                )
            )
            exerciseDefinitions.add(null)
            Log.d(TAG, "Added new empty exercise slot to workout ID: $id")
        } ?: Log.e(TAG, "Cannot add exercise, workoutId is null")
    }

    fun deleteExercise(exercise: WorkoutExercise) {
        val index = workoutExercises.indexOf(exercise)
        if (index != -1) {
            workoutExercises.removeAt(index)
            exerciseDefinitions.removeAt(index)
        }
    }

    fun onAddSet(exerciseId: UUID) {
        val exerciseIndex = workoutExercises.indexOfFirst { it.id == exerciseId }
        if (exerciseIndex == -1) return

        val definition = exerciseDefinitions.getOrNull(exerciseIndex)
        if (definition == null) {
            Log.e(TAG, "Cannot add set, ExerciseDefinition is null for this slot.")
            return
        }

        val newSet = createDefaultSetForLogType(definition.logType)
        val updatedSets = workoutExercises[exerciseIndex].sets.toMutableList().apply { add(newSet) }
        workoutExercises[exerciseIndex] = workoutExercises[exerciseIndex].copy(sets = updatedSets)
    }

    fun deleteSet(exerciseId: UUID, set: ExerciseSet) {
        val exerciseIndex = workoutExercises.indexOfFirst { it.id == exerciseId }
        if (exerciseIndex != -1) {
            val currentSets = workoutExercises[exerciseIndex].sets.toMutableList()
            currentSets.remove(set)
            workoutExercises[exerciseIndex] =
                workoutExercises[exerciseIndex].copy(sets = currentSets)
        }
    }

    private fun createDefaultSetForLogType(logType: LogType): ExerciseSet {
        return when (logType) {
            LogType.WEIGHT_AND_REPS -> WeightAndRepsSet()
            LogType.REPS_ONLY -> RepsOnlySet()
            LogType.TIME -> TimedSet()
            LogType.DISTANCE_AND_TIME -> DistanceAndTimeSet()
        }
    }

    fun onSetChanged(exerciseId: UUID, setIndex: Int, updatedSet: ExerciseSet) {
        val index = workoutExercises.indexOfFirst { it.id == exerciseId }
        if (index != -1) {
            val updatedSets = workoutExercises[index].sets.toMutableList()
            if (setIndex >= 0 && setIndex < updatedSets.size) {
                updatedSets[setIndex] = updatedSet
                workoutExercises[index] = workoutExercises[index].copy(sets = updatedSets)
            }
        }
    }

    fun onExerciseUnitChange(exerciseId: UUID, newUnit: WeightUnit) {
        val index = workoutExercises.indexOfFirst { it.id == exerciseId }
        if (index != -1) {
            workoutExercises[index] = workoutExercises[index].copy(unit = newUnit)
        }
    }

    fun onFinishWorkout(onNavigateBack: () -> Unit) {
        Log.d(TAG, "onFinishWorkout called.")

        currentWorkoutId?.let { id ->
            viewModelScope.launch {
                val exercisesToSave = workoutExercises.filter { exercise ->
                    exerciseDefinitions.any { def -> def?.id == exercise.definitionId }
                }

                Log.d(TAG, "Current workout count is: ${workoutRepository.getAllWorkoutCount()}")
                if (exercisesToSave.isNotEmpty()) {
                    workoutRepository.insertWorkoutExercises(exercisesToSave)
                    Log.d(TAG, "Saved ${exercisesToSave.size} exercises for workout ID: $id")
                }
                val workoutToUpdate = workoutRepository.getWorkout(id)
                workoutToUpdate?.let { workout ->
                    val updatedWorkout = workout.copy(
                        durationSeconds = secondsElapsed
                        // TODO: Get workout notes, if any
                    )
                    workoutRepository.updateWorkout(updatedWorkout)
                    Log.d(TAG, "Updated final duration for workout ID: $id")
                } ?: Log.e(
                    TAG,
                    "Could not update workout because it was not found in the database (ID: $id)"
                )
                Log.d(TAG, "New workout count is: ${workoutRepository.getAllWorkoutCount()}")
                onNavigateBack()
            }
        } ?: Log.e(TAG, "Cannot finish workout, workoutId is null.")
    }

    fun onDiscardWorkout(onNavigateBack: () -> Unit) {
        Log.d(TAG, "onDiscardWorkout called.")
        currentWorkoutId?.let { id ->
            viewModelScope.launch {
                Log.d(TAG, "Workout count before delete: ${workoutRepository.getAllWorkoutCount()}.")
                workoutRepository.getWorkout(id)?.let { workoutToDelete ->
                    workoutRepository.deleteWorkout(workoutToDelete)
                    Log.d(TAG, "Discarded and deleted workout ID: $id")
                }
                Log.d(TAG, "Workout count after delete: ${workoutRepository.getAllWorkoutCount()}.")
                onNavigateBack()
            }
        } ?: onNavigateBack()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

