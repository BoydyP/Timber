package com.android.timberworkoutlogs.ui.screen.workout

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.timberworkoutlogs.database.ExerciseDefinitionRepository
import com.android.timberworkoutlogs.database.SettingsRepository
import com.android.timberworkoutlogs.database.WorkoutRepository
import com.android.timberworkoutlogs.database.WorkoutTemplateRepository
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
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExerciseCount
import com.android.timberworkoutlogs.services.TimerService
import com.android.timberworkoutlogs.services.WorkoutStateHolder
import com.android.timberworkoutlogs.util.WeightUnitConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

private const val TAG = "WorkoutViewModel"

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val workoutTemplateRepository: WorkoutTemplateRepository,
    private val exerciseDefinitionRepository: ExerciseDefinitionRepository,
    private val settingsRepository: SettingsRepository,
    private val workoutStateHolder: WorkoutStateHolder,
    private val application: Application
) : ViewModel() {
    var timerService: TimerService? by mutableStateOf(null)
    private var isBound by mutableStateOf(false)
    private var startTimerWhenReady = false
    private var isInitializingSession = false
    private val sessionSwitchMutex = Mutex()

    private val _timerText = MutableStateFlow("00:00:00")
    val timerText = _timerText.asStateFlow()

    private var serviceConnection: ServiceConnection? = null
    private val collectionJobs = mutableListOf<Job>()

    private fun newServiceConnection() = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            val boundService = binder.getService()
            timerService = boundService
            isBound = true
            if (startTimerWhenReady) {
                boundService.startTimer()
                startTimerWhenReady = false
            }
            cancelCollectionJobs()
            collectionJobs += viewModelScope.launch {
                boundService.isTimerRunning.collect { workoutStateHolder.setTimerRunning(it) }
            }
            collectionJobs += viewModelScope.launch {
                boundService.timerText.collect { _timerText.value = it }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            timerService = null
            cancelCollectionJobs()
            workoutStateHolder.setTimerRunning(false)
        }
    }

    private fun cancelCollectionJobs() {
        collectionJobs.forEach { it.cancel() }
        collectionJobs.clear()
    }

    private fun bindTimerService() {
        if (isBound || serviceConnection != null) return
        val connection = newServiceConnection()
        serviceConnection = connection
        Intent(application, TimerService::class.java).also { intent ->
            application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindTimerService() {
        val connection = serviceConnection ?: return
        if (isBound) {
            application.unbindService(connection)
        }
        serviceConnection = null
        isBound = false
        cancelCollectionJobs()
    }

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
    private var currentWorkoutId: Long? = null
    val templates: StateFlow<List<WorkoutTemplateWithExerciseCount>> =
        workoutTemplateRepository.getAllTemplatesWithExerciseCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        Log.d(TAG, "ViewModel initialized")
        startNewWorkoutSession()
        bindTimerService()
    }

    override fun onCleared() {
        unbindTimerService()
        workoutStateHolder.setTimerRunning(false)
        super.onCleared()
    }

    fun startTimer() {
        if (isBound) {
            timerService?.startTimer()
        } else {
            startTimerWhenReady = true
        }
    }

    private fun startNewWorkoutSession() {
        if (isInitializingSession) {
            Log.d(TAG, "Workout session initialization already in progress, skipping")
            return
        }
        
        isInitializingSession = true
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val newWorkout = Workout(startTime = currentTime)
                val id = workoutRepository.insertWorkout(newWorkout)
                currentWorkoutId = id
                Log.d(TAG, "New workout session started with ID: $id")
                onAddExercise()
            } finally {
                isInitializingSession = false
            }
        }
    }

    fun onExerciseSelected(exerciseIndex: Int, definitionId: UUID) {
        // Resolve the target slot by its stable id, captured before the suspend point below,
        // since the list can be mutated (deleted/added) while the definition is loading.
        val targetExerciseId = workoutExercises.getOrNull(exerciseIndex)?.id ?: return
        viewModelScope.launch {
            val definition = exerciseDefinitionRepository.getExerciseDefinition(definitionId)
            val currentIndex = workoutExercises.indexOfFirst { it.id == targetExerciseId }
            if (currentIndex != -1) {
                val initialSet = createDefaultSetForLogType(definition.logType)

                exerciseDefinitions[currentIndex] = definition
                workoutExercises[currentIndex] = workoutExercises[currentIndex].copy(
                    definitionId = definitionId,
                    sets = listOf(initialSet)
                )
            }
        }
    }

    fun onAddExercise() {
        viewModelScope.launch {
            currentWorkoutId?.let { id ->
                val placeholderDefinitionId = UUID.randomUUID()
                val defaultUnit = settingsRepository.weightUnit.first()
                workoutExercises.add(
                    WorkoutExercise(
                        workoutId = id,
                        definitionId = placeholderDefinitionId,
                        sets = listOf(),
                        unit = defaultUnit
                    )
                )
                exerciseDefinitions.add(null)
                Log.d(TAG, "Added new empty exercise slot to workout ID: $id. Unit is $defaultUnit")
            } ?: Log.e(TAG, "Cannot add exercise, workoutId is null")
        }
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
        var newSet = createDefaultSetForLogType(definition.logType)
        if (definition.logType == LogType.WEIGHT_AND_REPS) {
            val lastSet = workoutExercises[exerciseIndex].sets.lastOrNull()
            if (lastSet is WeightAndRepsSet) {
                newSet = lastSet.copy(reps = 0, isDone = false)
            }
        }
        val updatedSets = workoutExercises[exerciseIndex].sets.toMutableList().apply { add(newSet) }
        workoutExercises[exerciseIndex] = workoutExercises[exerciseIndex].copy(sets = updatedSets)
    }
    fun deleteSet(exerciseId: UUID, setIndex: Int) {
        val exerciseIndex = workoutExercises.indexOfFirst { it.id == exerciseId }
        if (exerciseIndex != -1) {
            val currentSets = workoutExercises[exerciseIndex].sets.toMutableList()
            if (setIndex >= 0 && setIndex < currentSets.size) {
                currentSets.removeAt(setIndex)
                workoutExercises[exerciseIndex] =
                    workoutExercises[exerciseIndex].copy(sets = currentSets)
            }
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
            val exercise = workoutExercises[index]
            val oldUnit = exercise.unit
            // Converting the unit label alone would silently reinterpret already-entered
            // weight values in the new unit (e.g. "100 kg" becoming "100 lb"), corrupting
            // the logged weight. Convert every existing set's weight so the numeric value
            // still represents the same physical weight under the new unit.
            val convertedSets = exercise.sets.map { set ->
                if (set is WeightAndRepsSet) {
                    set.copy(weight = WeightUnitConverter.convert(set.weight, oldUnit, newUnit))
                } else {
                    set
                }
            }
            workoutExercises[index] = exercise.copy(unit = newUnit, sets = convertedSets)
        }
        Log.d(TAG, "Exercise unit changed to $newUnit for exercise with ID: $exerciseId")
    }

    fun onFinishWorkout(onNavigateBack: () -> Unit) {
        Log.d(TAG, "onFinishWorkout called.")
        val seconds = timerService?.getSecondsElapsed() ?: 0

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
                        durationSeconds = seconds
                    )
                    workoutRepository.updateWorkout(updatedWorkout)
                    Log.d(TAG, "Updated final duration for workout ID: $id")
                } ?: Log.e(
                    TAG,
                    "Could not update workout because it was not found in the database (ID: $id)"
                )
                Log.d(TAG, "New workout count is: ${workoutRepository.getAllWorkoutCount()}")
                
                resetWorkoutSession()
                onNavigateBack()
            }
        } ?: Log.e(TAG, "Cannot finish workout, workoutId is null.")
    }

    fun onDiscardWorkout(onNavigateBack: () -> Unit) {
        Log.d(TAG, "onDiscardWorkout called.")
        val id = currentWorkoutId
        if (id == null) {
            resetWorkoutSession()
            onNavigateBack()
            return
        }
        viewModelScope.launch {
            Log.d(TAG, "Workout count before delete: ${workoutRepository.getAllWorkoutCount()}.")
            workoutRepository.getWorkout(id)?.let { workoutToDelete ->
                workoutRepository.deleteWorkout(workoutToDelete)
                Log.d(TAG, "Discarded and deleted workout ID: $id")
            }
            Log.d(TAG, "Workout count after delete: ${workoutRepository.getAllWorkoutCount()}.")

            resetWorkoutSession()
            onNavigateBack()
        }
    }

    fun importExercisesFromTemplate(templateId: Long) {
        viewModelScope.launch {
            val templateWithExercises =
                workoutTemplateRepository.getTemplateWithExercises(templateId)
            // Remove the placeholder exercise
            if (workoutExercises.size == 1 && exerciseDefinitions.firstOrNull() == null) {
                workoutExercises.clear()
                exerciseDefinitions.clear()
            }

            templateWithExercises.exercises.forEach { templateExercise ->
                val definition =
                    exerciseDefinitionRepository.getExerciseDefinition(templateExercise.definitionId)
                val newWorkoutExercise = WorkoutExercise(
                    workoutId = currentWorkoutId!!,
                    definitionId = templateExercise.definitionId,
                    sets = templateExercise.sets,
                    unit = settingsRepository.weightUnit.first()
                )
                workoutExercises.add(newWorkoutExercise)
                exerciseDefinitions.add(definition)
            }
        }
    }

    /**
     * Resets the workout session state while keeping the ViewModel alive.
     * This is called when finishing or discarding a workout to ensure proper cleanup
     * while maintaining the hoisted architecture.
     */
    private fun resetWorkoutSession() {
        Log.d(TAG, "resetWorkoutSession called - cleaning up workout state")

        timerService?.stopTimer()
        unbindTimerService()

        _timerText.value = "00:00"
        workoutStateHolder.setTimerRunning(false)
        
        workoutExercises.clear()
        exerciseDefinitions.clear()
        currentWorkoutId = null
        
        startTimerWhenReady = false
        isInitializingSession = false
        
        Log.d(TAG, "Workout session reset completed")
    }

    /**
     * Starts a new workout session if one is not already active, or switches to the
     * workout identified by [workoutId] (e.g. one just created from a template).
     * This ensures we have the right session active when navigating to the workout screen.
     */
    fun ensureWorkoutSession(workoutId: Long? = null) {
        Log.d(TAG, "ensureWorkoutSession called, workoutId: $workoutId, currentWorkoutId: $currentWorkoutId, isInitializing: $isInitializingSession")

        if (workoutId != null && workoutId != currentWorkoutId) {
            switchToWorkoutSession(workoutId)
            return
        }

        if (currentWorkoutId == null && !isInitializingSession) {
            Log.d(TAG, "No active workout session, starting new one")
            startNewWorkoutSession()
            bindTimerService()
        } else if (currentWorkoutId != null) {
            Log.d(TAG, "Active workout session already exists with ID: $currentWorkoutId")
        } else {
            Log.d(TAG, "Workout session initialization in progress, skipping")
        }
    }

    /**
     * Switches the active session to an already-persisted workout (e.g. one created via
     * [importExercisesFromTemplate]'s sibling, `createWorkoutFromTemplate`). The previously
     * active session is only discarded if it was still an untouched placeholder, so a session
     * the user was actively logging into is never silently dropped.
     */
    private fun switchToWorkoutSession(workoutId: Long) {
        viewModelScope.launch {
            sessionSwitchMutex.withLock {
                if (workoutId == currentWorkoutId) return@withLock

                currentWorkoutId?.let { staleId ->
                    if (isWorkoutEmpty.value) {
                        workoutRepository.getWorkout(staleId)
                            ?.let { workoutRepository.deleteWorkout(it) }
                    }
                }

                val exercises = workoutRepository.getExercisesForWorkout(workoutId)
                workoutExercises.clear()
                exerciseDefinitions.clear()
                exercises.forEach { exercise ->
                    workoutExercises.add(exercise)
                    exerciseDefinitions.add(
                        exerciseDefinitionRepository.getExerciseDefinition(exercise.definitionId)
                    )
                }
                currentWorkoutId = workoutId
                Log.d(TAG, "Switched to workout session with ID: $workoutId (${exercises.size} exercises)")
            }
        }
        bindTimerService()
    }
}
