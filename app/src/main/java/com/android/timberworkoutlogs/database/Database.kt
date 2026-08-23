package com.android.timberworkoutlogs.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.timberworkoutlogs.database.converters.ExerciseEquipmentConverter
import com.android.timberworkoutlogs.database.converters.ExerciseSetListConverter
import com.android.timberworkoutlogs.database.converters.LogTypeConverter
import com.android.timberworkoutlogs.database.converters.MuscleGroupListConverter
import com.android.timberworkoutlogs.database.converters.UUIDConverter
import com.android.timberworkoutlogs.database.converters.WeightUnitConverter
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.Workout
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.models.WorkoutTemplate

@Database(
    entities = [
        ExerciseDefinition::class,
        Workout::class,
        WorkoutExercise::class,
        WorkoutTemplate::class,
        TemplateExercise::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(
    UUIDConverter::class,
    ExerciseSetListConverter::class,
    ExerciseEquipmentConverter::class,
    MuscleGroupListConverter::class,
    LogTypeConverter::class,
    WeightUnitConverter::class
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDefinitionDao(): ExerciseDefinitionDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL
                    )
                """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `template_exercises` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `templateId` INTEGER NOT NULL,
                        `definitionId` TEXT NOT NULL,
                        `unit` TEXT NOT NULL,
                        `sets` TEXT NOT NULL,
                        FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`definitionId`) REFERENCES `exercise_definitions`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_templateId` ON `template_exercises` (`templateId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_definitionId` ON `template_exercises` (`definitionId`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a new table with the desired schema
                db.execSQL(
                    """
                    CREATE TABLE `template_exercises_new` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `templateId` INTEGER NOT NULL,
                        `definitionId` TEXT NOT NULL,
                        `sets` TEXT NOT NULL,
                        FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`definitionId`) REFERENCES `exercise_definitions`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `template_exercises_new` (id, templateId, definitionId, sets)
                    SELECT id, templateId, definitionId, sets FROM `template_exercises`
                """.trimIndent()
                )
                db.execSQL("DROP TABLE `template_exercises`")
                db.execSQL("ALTER TABLE `template_exercises_new` RENAME TO `template_exercises`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_templateId` ON `template_exercises` (`templateId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_template_exercises_definitionId` ON `template_exercises` (`definitionId`)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `template_exercises` ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Note: this deliberately does not seed. Room's `onCreate` callback fires exactly
         * once per install and cannot suspend, which forced seeding to be fire-and-forget
         * and unobservable. `DatabaseInitializer` owns seeding instead, so it can be
         * awaited and re-run.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timber_database.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
