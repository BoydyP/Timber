package com.android.timberworkoutlogs.rules

import com.android.timberworkoutlogs.database.AppDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A JUnit rule that ensures each test class starts with a fresh database seeded with production data.
 * 
 * This rule:
 * - Clears all database tables before the test class runs
 * - Re-seeds the database with DatabaseSeeder.seedProdData()
 * - Ensures consistent starting state for all tests in a class
 * 
 * Usage:
 * ```
 * @HiltAndroidTest
 * class MyTestClass : TestCase() {
 *     @get:Rule(order = 0)
 *     val hiltRule = HiltAndroidRule(this)
 *     
 *     @get:Rule(order = 1)
 *     val databaseSeedingRule = DatabaseSeedingRule()
 *     
 *     @get:Rule(order = 2)
 *     val composeTestRule = createAndroidComposeRule<MainActivity>()
 * }
 * ```
 */
class DatabaseSeedingRule : TestRule {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DatabaseSeedingEntryPoint {
        fun getDatabase(): AppDatabase
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                // Always clear and reseed for now to ensure fresh data
                // TODO: Optimize to only seed once per test class if needed
                clearAndReseedDatabase()
                
                // Run the actual tests
                base.evaluate()
            }
        }
    }

    private fun clearAndReseedDatabase() {
        runBlocking {
            // Get database instance through Hilt EntryPoint
            val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
            val entryPoint = EntryPointAccessors.fromApplication(context, DatabaseSeedingEntryPoint::class.java)
            val database = entryPoint.getDatabase()
            
            // Use Room's built-in clearAllTables() which properly handles foreign key constraints
            database.clearAllTables()
            
            // Re-seed with fresh production data synchronously
            seedProdDataSynchronously(database)
            
            // Add a small delay to ensure data is fully committed and available to UI
            delay(100)
        }
    }
    
    /**
     * Synchronous version of DatabaseSeeder.seedProdData() to ensure seeding completes before tests run
     */
    private suspend fun seedProdDataSynchronously(db: AppDatabase) {
        val exerciseDefDao = db.exerciseDefinitionDao()
        val templateDao = db.workoutTemplateDao()

        // Seed Default Exercises
        val defaultExercises = com.android.timberworkoutlogs.database.data.DefaultExercises.getPredefinedExercises()
        android.util.Log.d("DatabaseSeedingRule", "Seeding ${defaultExercises.size} default exercises")
        
        defaultExercises.forEach { exercise ->
            try {
                exerciseDefDao.addExerciseDefinition(exercise)
                android.util.Log.d("DatabaseSeedingRule", "Added exercise: ${exercise.name} (${exercise.equipment})")
            } catch (e: Exception) {
                android.util.Log.e("DatabaseSeedingRule", "Failed to add exercise: ${exercise.name}", e)
            }
        }

        // Seed Default Templates
        val templatesWithExercises = com.android.timberworkoutlogs.database.data.DefaultTemplates.getTemplatesWithExercises(defaultExercises)
        android.util.Log.d("DatabaseSeedingRule", "Seeding ${templatesWithExercises.size} default templates")
        
        templatesWithExercises.forEach { (template, exercises) ->
            try {
                val templateId = templateDao.insertTemplate(template)
                val exercisesWithCorrectId = exercises.map { it.copy(templateId = templateId) }
                templateDao.upsertTemplateExercises(exercisesWithCorrectId)
                android.util.Log.d("DatabaseSeedingRule", "Added template: ${template.name} with ${exercises.size} exercises")
            } catch (e: Exception) {
                android.util.Log.e("DatabaseSeedingRule", "Failed to add template: ${template.name}", e)
            }
        }
        
        android.util.Log.d("DatabaseSeedingRule", "Seeding process completed")
    }
}
