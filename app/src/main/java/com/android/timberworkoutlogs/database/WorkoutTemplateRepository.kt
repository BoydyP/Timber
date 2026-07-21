package com.android.timberworkoutlogs.database

import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExerciseCount
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExercises
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WorkoutTemplateRepository(
    private val workoutTemplateDao: WorkoutTemplateDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun getTemplateWithExercises(templateId: Long): WorkoutTemplateWithExercises {
        // Room's @Relation fetch (see WorkoutTemplateDao) has no ORDER BY on the child list,
        // so the user's saved exercise order must be restored here for every consumer.
        val templateWithExercises = workoutTemplateDao.getTemplateWithExercises(templateId)
        return templateWithExercises.copy(
            exercises = templateWithExercises.exercises.sortedBy { it.order }
        )
    }

    fun getAllTemplatesWithExerciseCount(): Flow<List<WorkoutTemplateWithExerciseCount>> {
        return workoutTemplateDao.getAllTemplatesWithExerciseCount()
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

    suspend fun replaceTemplateExercises(templateId: Long, exercises: List<TemplateExercise>) {
        workoutTemplateDao.replaceTemplateExercises(templateId, exercises)
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
        // 1. Fetch the complete template with its exercises, in the user's saved order.
        val templateWithExercises = getTemplateWithExercises(templateId)

        // 2. Create a new Workout instance.
        val newWorkout = Workout(name = templateWithExercises.template.name)
        val newWorkoutId = workoutDao.insertWorkout(newWorkout)

        // TemplateExercise has no unit of its own (see TemplateExercise.kt), so the new
        // WorkoutExercises must be tagged with the user's current weight-unit preference.
        // Without this, WorkoutExercise's default (WeightUnit.KG) is used regardless of the
        // user's actual setting, silently mislabeling the weight unit for LB users.
        val currentUnit = settingsRepository.weightUnit.first()

        // 3. Convert every TemplateExercise into a new WorkoutExercise.
        val newWorkoutExercises = templateWithExercises.exercises.map { templateExercise ->
            WorkoutExercise(
                workoutId = newWorkoutId,
                definitionId = templateExercise.definitionId,
                unit = currentUnit,
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
