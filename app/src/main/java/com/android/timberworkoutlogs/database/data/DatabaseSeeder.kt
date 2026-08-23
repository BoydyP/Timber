package com.android.timberworkoutlogs.database.data

import androidx.room.withTransaction
import com.android.timberworkoutlogs.database.AppDatabase
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
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Populates the database. Two deliberately separate jobs:
 * - [seedCatalog] writes reference content that every user needs: the built-in
 *   exercise definitions and workout templates. It runs in every build.
 * - [seedDemoHistory] writes fake workout history so charts and stats screens have
 *   something to render while developing. It is only reachable from debug builds, on
 *   demand, and never runs automatically.
 */
object DatabaseSeeder {

    /** How many days of history [seedDemoHistory] generates by default. */
    const val DEMO_HISTORY_DAYS = 60

    /** Fixed seed, so a generated demo database is identical every time. */
    private const val DEMO_RANDOM_SEED = 42L

    /** Smallest increment achievable with real plates, in kg. */
    private const val WEIGHT_INCREMENT_KG = 2.5

    private const val SETS_PER_EXERCISE = 3
    private const val EXERCISES_PER_WORKOUT = 5

    /**
     * Inserts the default exercises and templates.
     */
    suspend fun seedCatalog(db: AppDatabase) = db.withTransaction {
        val exerciseDefDao = db.exerciseDefinitionDao()
        val templateDao = db.workoutTemplateDao()

        val defaultExercises = DefaultExercises.getPredefinedExercises()
        defaultExercises.forEach { exerciseDefDao.addExerciseDefinition(it) }

        DefaultTemplates.getTemplatesWithExercises(defaultExercises)
            .forEach { (template, exercises) ->
                val templateId = templateDao.insertTemplate(template)
                templateDao.upsertTemplateExercises(
                    exercises.mapIndexed { index, exercise ->
                        exercise.copy(templateId = templateId, order = index)
                    }
                )
            }
    }

    /**
     * Generates [days] of push/pull/legs history ending today.
     *
     * Exercises are read back from the database rather than regenerated, because
     * `WorkoutExercise.definitionId` is a foreign key — the history has to point at rows
     * that genuinely exist, whatever their ids happen to be.
     *
     * @param random supply a fixed-seed [Random] (the default) for a reproducible
     *   database, or a fresh one for varied data.
     */
    suspend fun seedDemoHistory(
        db: AppDatabase,
        days: Int = DEMO_HISTORY_DAYS,
        random: Random = Random(DEMO_RANDOM_SEED)
    ) {
        val availableExercises = db.exerciseDefinitionDao().getExerciseDefinitions().first()

        val routine = listOf(
            "Push Day" to availableExercises.filter { it.name in PUSH_EXERCISES },
            "Pull Day" to availableExercises.filter { it.name in PULL_EXERCISES },
            "Leg Day" to availableExercises.filter { it.name in LEG_EXERCISES }
        ).filter { (_, exercises) -> exercises.isNotEmpty() }

        if (routine.isEmpty()) return

        db.withTransaction {
            val workoutDao = db.workoutDao()
            val workoutExerciseDao = db.workoutExerciseDao()
            val startDate = LocalDate.now().minusDays(days.toLong())

            for (dayOffset in 0 until days) {
                val currentDate = startDate.plusDays(dayOffset.toLong())
                val (workoutName, candidates) = routine[dayOffset % routine.size]

                val workoutId = workoutDao.insertWorkout(
                    Workout(
                        name = workoutName,
                        startTime = currentDate.atStartOfDay()
                            .toInstant(ZoneOffset.UTC)
                            .toEpochMilli(),
                        durationSeconds = demoDurationSeconds(random),
                        notes = "Demo workout on $currentDate"
                    )
                )

                val workoutExercises = candidates
                    .shuffled(random)
                    .take(EXERCISES_PER_WORKOUT)
                    .map { exerciseDef ->
                        WorkoutExercise(
                            workoutId = workoutId,
                            definitionId = exerciseDef.id,
                            sets = demoSets(exerciseDef, random),
                            unit = WeightUnit.KG
                        )
                    }

                if (workoutExercises.isNotEmpty()) {
                    workoutExerciseDao.insertWorkoutExercises(workoutExercises)
                }
            }
        }
    }

    private fun demoDurationSeconds(random: Random): Int {
        val millis = TimeUnit.HOURS.toMillis(1) +
                random.nextLong(TimeUnit.MINUTES.toMillis(30))
        return TimeUnit.MILLISECONDS.toSeconds(millis).toInt()
    }

    private fun demoSets(exerciseDef: ExerciseDefinition, random: Random): List<ExerciseSet> =
        List(SETS_PER_EXERCISE) {
            when (exerciseDef.logType) {
                LogType.WEIGHT_AND_REPS -> WeightAndRepsSet(
                    reps = random.nextInt(5, 13),
                    weight = plateWeightKg(random, minKg = 20.0, maxKg = 100.0),
                    isDone = true
                )

                LogType.TIME -> TimedSet(
                    durationSeconds = random.nextInt(30, 181),
                    isDone = true
                )

                LogType.DISTANCE_AND_TIME -> DistanceAndTimeSet(
                    distance = roundTo(random.nextDouble(1.0, 5.0), decimalPlaces = 1),
                    durationSeconds = random.nextInt(300, 1801),
                    isDone = true
                )

                LogType.REPS_ONLY -> RepsOnlySet(
                    reps = random.nextInt(8, 21),
                    isDone = true
                )
            }
        }

    /**
     * A weight a lifter could actually load, i.e. a multiple of [WEIGHT_INCREMENT_KG].
     * Generating raw doubles here is what produced values like `52.562496` in demo data,
     * which look wrong to a user and stress unit conversion for no good reason.
     */
    private fun plateWeightKg(random: Random, minKg: Double, maxKg: Double): Double =
        (random.nextDouble(minKg, maxKg) / WEIGHT_INCREMENT_KG).roundToInt() * WEIGHT_INCREMENT_KG

    private fun roundTo(value: Double, decimalPlaces: Int): Double {
        var factor = 1.0
        repeat(decimalPlaces) { factor *= 10 }
        return (value * factor).roundToInt() / factor
    }

    private val PUSH_EXERCISES = setOf(
        "Bench Press",
        "Incline Bench Press",
        "Fly",
        "Chest Press",
        "Push Up",
        "Dip",
        "Overhead Press",
        "Shoulder Press",
        "Arnold Press",
        "Lateral Raise",
        "Front Raise",
        "Tricep Pushdown",
        "Skull Crusher",
        "Overhead Tricep Extension",
        "Close Grip Bench Press"
    )

    private val PULL_EXERCISES = setOf(
        "Deadlift",
        "Bent Over Row",
        "Pull Up",
        "Lat Pulldown",
        "Seated Row",
        "T-Bar Row",
        "Pullover",
        "Shrug",
        "Face Pull",
        "Bicep Curl",
        "Hammer Curl",
        "Preacher Curl",
        "Chin Up"
    )

    private val LEG_EXERCISES = setOf(
        "Squat",
        "Goblet Squat",
        "Leg Press",
        "Lunge",
        "Romanian Deadlift",
        "Leg Extension",
        "Leg Curl",
        "Calf Raise",
        "Swing"
    )
}
