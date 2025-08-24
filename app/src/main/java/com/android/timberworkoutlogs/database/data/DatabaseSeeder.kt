package com.android.timberworkoutlogs.database.data

import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * A single seeder to handle all database population.
 * Choose which function to call in AppDatabase based on build needs.
 */
object DatabaseSeeder {

    /**
     * "Prod" seeder. Populates only default exercises and templates.
     */
    fun seedProdData(db: AppDatabase) {
        val exerciseDefDao = db.exerciseDefinitionDao()
        val templateDao = db.workoutTemplateDao()

        CoroutineScope(Dispatchers.IO).launch {
            // Seed Default Exercises
            val defaultExercises = DefaultExercises.getPredefinedExercises()
            defaultExercises.forEach { exerciseDefDao.addExerciseDefinition(it) }

            // Seed Default Templates
            val templatesWithExercises =
                DefaultTemplates.getTemplatesWithExercises(defaultExercises)
            templatesWithExercises.forEach { (template, exercises) ->
                val templateId = templateDao.insertTemplate(template)
                val exercisesWithCorrectId = exercises.map { it.copy(templateId = templateId) }
                templateDao.upsertTemplateExercises(exercisesWithCorrectId)
            }
        }
    }

    /**
     * "Dev" seeder. Populates exercises, templates, AND 90 days of realistic workout history.
     */
    fun seedRealisticData(db: AppDatabase) {
        val exerciseDefDao = db.exerciseDefinitionDao()
        val templateDao = db.workoutTemplateDao()
        val workoutDao = db.workoutDao()
        val workoutExerciseDao = db.workoutExerciseDao()

        CoroutineScope(Dispatchers.IO).launch {
            // 1. Seed Default Exercises & Templates
            val defaultExercises = DefaultExercises.getPredefinedExercises()
            defaultExercises.forEach { exerciseDefDao.addExerciseDefinition(it) }
            val templatesWithExercises =
                DefaultTemplates.getTemplatesWithExercises(defaultExercises)
            templatesWithExercises.forEach { (template, exercises) ->
                val templateId = templateDao.insertTemplate(template)
                val exercisesWithCorrectId = exercises.map { it.copy(templateId = templateId) }
                templateDao.upsertTemplateExercises(exercisesWithCorrectId)
            }

            // 2. Seed Realistic Workout History
            val startDate = LocalDate.now().minusDays(90)
            for (i in 0 until 90) {
                val currentDate = startDate.plusDays(i.toLong())
                val workoutTimestamp =
                    currentDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

                val workout = Workout(
                    name = "Realistic Workout on $currentDate",
                    startTime = workoutTimestamp,
                    durationSeconds = (TimeUnit.HOURS.toMillis(1) + Random.nextLong(
                        TimeUnit.MINUTES.toMillis(
                            30
                        )
                    )).toInt() / 1000,
                    notes = "Notes for workout on $currentDate"
                )
                val workoutId = workoutDao.insertWorkout(workout)

                val exercisesForThisWorkout = defaultExercises.shuffled().take(3)
                val workoutExercises = mutableListOf<WorkoutExercise>()

                exercisesForThisWorkout.forEach { exerciseDef ->
                    val sets = mutableListOf<ExerciseSet>()
                    for (setNum in 1..3) {
                        val set: ExerciseSet = when (exerciseDef.logType) {
                            LogType.WEIGHT_AND_REPS -> WeightAndRepsSet(
                                reps = Random.nextInt(
                                    5,
                                    13
                                ), weight = Random.nextDouble(20.0, 100.0), isDone = true
                            )

                            LogType.TIME -> TimedSet(
                                durationSeconds = Random.nextInt(30, 181),
                                isDone = true
                            )

                            LogType.DISTANCE_AND_TIME -> DistanceAndTimeSet(
                                distance = Random.nextDouble(
                                    1.0,
                                    5.0
                                ), durationSeconds = Random.nextInt(300, 1801), isDone = true
                            )

                            LogType.REPS_ONLY -> RepsOnlySet(
                                reps = Random.nextInt(8, 21),
                                isDone = true
                            )
                        }
                        sets.add(set)
                    }
                    workoutExercises.add(
                        WorkoutExercise(
                            workoutId = workoutId,
                            definitionId = exerciseDef.id,
                            sets = sets,
                            unit = WeightUnit.KG
                        )
                    )
                }
                if (workoutExercises.isNotEmpty()) {
                    workoutExerciseDao.insertWorkoutExercises(workoutExercises)
                }
            }
        }
    }
}
