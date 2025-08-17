import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.database.data.DefaultExercises
import com.android.timberworkoutlogs.database.data.DefaultTemplates
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DatabaseSeedingTest {

    @Test
    fun seedDatabaseAndCreateFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("timber_database.db")

        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "timber_database.db"
        ).build()
        runBlocking {
            val exerciseDao = db.exerciseDefinitionDao()
            val defaultExercises = DefaultExercises.getPredefinedExercises()
            defaultExercises.forEach { exerciseDao.addExerciseDefinition(it) }

            val templateDao = db.workoutTemplateDao()
            val templatesWithExercises =
                DefaultTemplates.getTemplatesWithExercises(defaultExercises)

            templatesWithExercises.forEach { (template, exercises) ->
                val templateId = templateDao.insertTemplate(template)
                val exercisesWithCorrectId = exercises.map { it.copy(templateId = templateId) }
                templateDao.upsertTemplateExercises(exercisesWithCorrectId)
            }
        }

        db.close()

        println("Database seeded. Waiting for 2 minutes before test finishes...")
        Thread.sleep(TimeUnit.MINUTES.toMillis(2))
        println("Test finished.")
    }
}
