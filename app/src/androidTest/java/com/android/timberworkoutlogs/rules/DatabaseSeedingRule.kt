package com.android.timberworkoutlogs.rules

import com.android.timberworkoutlogs.database.AppDatabase
import com.android.timberworkoutlogs.database.data.DatabaseSeeder
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
 * A JUnit rule that ensures each test class starts with a fresh database seeded with the
 * default exercise and template catalog.
 *
 * This rule:
 * - Clears all database tables before the test class runs
 * - Re-seeds the database with DatabaseSeeder.seedCatalog()
 * - Ensures consistent starting state for all tests in a class
 *
 * Note this seeds the catalog only, never workout history. Tests that need history should
 * insert exactly the workouts they assert on.
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

            // Re-seed with the same catalog the app itself uses. DatabaseSeeder.seedCatalog
            // is a suspend function, so it can simply be awaited here — this rule used to
            // keep its own copy of the seeding logic, which had already drifted out of sync.
            DatabaseSeeder.seedCatalog(database)

            // Add a small delay to ensure data is fully committed and available to UI
            delay(100)
        }
    }
}
