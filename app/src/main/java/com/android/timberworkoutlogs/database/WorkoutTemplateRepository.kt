package com.android.timberworkoutlogs.database

import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExercises
import kotlinx.coroutines.flow.Flow

class WorkoutTemplateRepository(
    private val workoutTemplateDao: WorkoutTemplateDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
) {
    fun getTemplatesWithExercises(): Flow<List<WorkoutTemplateWithExercises>> {
        return workoutTemplateDao.getTemplatesWithExercises()
    }

    suspend fun getTemplateWithExercises(templateId: Long): WorkoutTemplateWithExercises {
        return workoutTemplateDao.getTemplateWithExercises(templateId)
    }

    fun getAllTemplates(): Flow<List<WorkoutTemplate>> {
        return workoutTemplateDao.getAllTemplates()
    }

    suspend fun insertTemplate(template: WorkoutTemplate): Long {
        return workoutTemplateDao.insertTemplate(template)
    }

    suspend fun updateTemplate(template: WorkoutTemplate) {
        workoutTemplateDao.updateTemplate(template)
    }

    suspend fun upsertTemplateExercises(exercises: List<TemplateExercise>) {
        workoutTemplateDao.upsertTemplateExercises(exercises)
    }

    suspend fun deleteExercisesForTemplate(templateId: Long) {
        workoutTemplateDao.deleteExercisesForTemplate(templateId)
    }

    suspend fun deleteTemplate(template: WorkoutTemplate) {
        workoutTemplateDao.deleteTemplate(template)
    }

    /**
     * Creates a new Workout and its associated WorkoutExercises from a given template.
     * @param templateId The ID of the WorkoutTemplate to use.
     * @return The ID of the newly created Workout.
     */
    suspend fun createWorkoutFromTemplate(templateId: Long): Long {
        // 1. Fetch the complete template with its exercises.
        val templateWithExercises = workoutTemplateDao.getTemplateWithExercises(templateId)

        // 2. Create a new Workout instance.
        val newWorkout = Workout(name = templateWithExercises.template.name)
        val newWorkoutId = workoutDao.insertWorkout(newWorkout)

        // 3. Convert every TemplateExercise into a new WorkoutExercise.
        val newWorkoutExercises = templateWithExercises.exercises.map { templateExercise ->
            WorkoutExercise(
                workoutId = newWorkoutId,
                definitionId = templateExercise.definitionId,
                unit = templateExercise.unit,
                // 4. Reset the 'isDone' flag for each set.
                sets = templateExercise.sets.map { set ->
                    when (set) {
                        is WeightAndRepsSet -> set.copy(isDone = false)
                        is RepsOnlySet -> set.copy(isDone = false)
                        is TimedSet -> set.copy(isDone = false)
                        is DistanceAndTimeSet -> set.copy(isDone = false)
                    }
                }
            )
        }

        // 5. Save the new workout exercises.
        workoutExerciseDao.insertWorkoutExercises(newWorkoutExercises)

        // 6. Return the ID of the new workout.
        return newWorkoutId
    }
}