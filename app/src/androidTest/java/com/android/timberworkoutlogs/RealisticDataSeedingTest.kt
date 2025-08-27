//package com.android.timberworkoutlogs
//
//import androidx.room.Room
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import com.android.timberworkoutlogs.database.AppDatabase
//import com.android.timberworkoutlogs.database.data.DefaultExercises
//import com.android.timberworkoutlogs.database.data.DefaultTemplates
//import com.android.timberworkoutlogs.models.ExerciseSet
//import com.android.timberworkoutlogs.models.LogType
//import com.android.timberworkoutlogs.models.WeightAndRepsSet
//import com.android.timberworkoutlogs.models.TimedSet
//import com.android.timberworkoutlogs.models.DistanceAndTimeSet
//import com.android.timberworkoutlogs.models.RepsOnlySet
//import com.android.timberworkoutlogs.models.WeightUnit // Precise import
//import com.android.timberworkoutlogs.models.Workout
//import com.android.timberworkoutlogs.models.WorkoutExercise
//import kotlinx.coroutines.runBlocking
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.time.LocalDate
//import java.time.ZoneOffset
//import java.util.UUID
//import java.util.concurrent.TimeUnit
//import kotlin.random.Random
//
//@RunWith(AndroidJUnit4::class)
//class RealisticDataSeedingTest {
//
//    @Test
//    fun seedDatabaseWithRealisticData() {
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//        val dbName = "timber_database.db"
//        context.deleteDatabase(dbName)
//
//        val db = Room.databaseBuilder(
//            context,
//            AppDatabase::class.java,
//            dbName
//        ).build()
//
//        runBlocking {
//            // 1. Seed Default Exercises & Templates (Prerequisite for realistic workouts)
//            val exerciseDefDao = db.exerciseDefinitionDao()
//            val defaultExercises = DefaultExercises.getPredefinedExercises()
//            defaultExercises.forEach { exerciseDefDao.addExerciseDefinition(it) }
//
//            val templateDao = db.workoutTemplateDao()
//            val templatesWithExercises =
//                DefaultTemplates.getTemplatesWithExercises(defaultExercises)
//
//            templatesWithExercises.forEach { (template, exercises) ->
//                val templateId = templateDao.insertTemplate(template)
//                val exercisesWithCorrectId = exercises.map { it.copy(templateId = templateId) }
//                templateDao.upsertTemplateExercises(exercisesWithCorrectId)
//            }
//
//            // 2. Seed Realistic Workout History for the past 90 days
//            val workoutDao = db.workoutDao()
//            val workoutExerciseDao = db.workoutExerciseDao() // Get WorkoutExerciseDao instance
//            val startDate = LocalDate.now().minusDays(90)
//            val exercisesPerWorkout = 3 // Average number of exercises per workout
//            val setsPerExercise = 3 // Average number of sets per exercise
//
//            for (i in 0 until 90) {
//                val currentDate = startDate.plusDays(i.toLong())
//                val workoutTimestamp =
//                    currentDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
//
//                val workout = Workout(
//                    id = 0, // Auto-generated
//                    name = "Realistic Workout on $currentDate",
//                    startTime = workoutTimestamp,
//                    durationSeconds = (TimeUnit.HOURS.toMillis(1) + Random.nextLong(
//                        TimeUnit.MINUTES.toMillis(
//                            30
//                        )
//                    )).toInt() / 1000,
//                    notes = "Notes for workout on $currentDate"
//                )
//                val workoutId = workoutDao.insertWorkout(workout)
//
//                val workoutExercises = mutableListOf<WorkoutExercise>()
//                // Get a shuffled sublist of default exercises to vary workouts
//                val exercisesForThisWorkout = defaultExercises.shuffled().take(exercisesPerWorkout)
//
//                exercisesForThisWorkout.forEach { exerciseDef ->
//                    val sets = mutableListOf<ExerciseSet>()
//                    for (setNum in 1..setsPerExercise) {
//                        val set: ExerciseSet = when (exerciseDef.logType) {
//                            LogType.WEIGHT_AND_REPS -> WeightAndRepsSet(
//                                reps = Random.nextInt(5, 13), // 5-12 reps
//                                weight = Random.nextDouble(20.0, 100.0),
//                                isDone = true
//                            )
//
//                            LogType.TIME -> TimedSet(
//                                durationSeconds = Random.nextInt(
//                                    30,
//                                    181
//                                ), // 30s to 3min. Changed from timeSeconds
//                                isDone = true
//                            )
//
//                            LogType.DISTANCE_AND_TIME -> DistanceAndTimeSet(
//                                distance = Random.nextDouble(1.0, 5.0),
//                                durationSeconds = Random.nextInt(
//                                    300,
//                                    1801
//                                ), // 5min to 30min. Changed from timeSeconds
//                                isDone = true
//                            )
//
//                            LogType.REPS_ONLY -> RepsOnlySet(
//                                reps = Random.nextInt(5, 13), // 5-12 reps
//                                isDone = true
//                            )
//
//                        }
//                        sets.add(set)
//                    }
//
//                    workoutExercises.add(
//                        WorkoutExercise(
//                            id = UUID.randomUUID(),
//                            workoutId = workoutId,
//                            definitionId = exerciseDef.id,
//                            sets = sets,
//                            unit = WeightUnit.KG // Corrected unit
//                        )
//                    )
//                }
//                workoutExerciseDao.insertWorkoutExercises(workoutExercises) // Called on workoutExerciseDao
//            }
//        }
//
//        db.close()
//
//        println("Database seeded with realistic data. Waiting for 2 minutes before test finishes to allow inspection...")
//        Thread.sleep(TimeUnit.MINUTES.toMillis(2)) // Keep DB alive for manual inspection if needed
//        println("Realistic data seeding test finished.")
//    }
//}
