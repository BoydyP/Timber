package com.android.timberworkoutlogs.database

import android.util.Log
import com.android.timberworkoutlogs.database.data.DatabaseSeeder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns database seeding.
 *
 * Seeding used to live in Room's `onCreate` callback, which fires exactly once per
 * install. That coupled three unrelated decisions together: *whether* to seed, *what* to
 * seed, and *when*. Doing it here instead means:
 *
 * - the reference catalog can be back-filled on any launch, not just a fresh install, so
 *   new default exercises reach existing users;
 * - callers can await it, rather than racing a detached coroutine;
 * - demo history becomes a separate, explicitly triggered action.
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val database: AppDatabase
) {

    private val mutex = Mutex()

    /**
     * Ensures the built-in exercises and templates exist. Safe to call on every launch —
     * it only writes when the catalog is missing.
     *
     * The lock makes the check-then-seed atomic. Without it, two concurrent callers (an
     * activity recreated during startup, say) could both see an empty catalog and each
     * insert a full set of templates, since template ids are auto-generated.
     */
    suspend fun ensureCatalogSeeded() {
        mutex.withLock {
            try {
                if (isInTestEnvironment()) return
                if (database.exerciseDefinitionDao().getExerciseCount() > 0) return
                DatabaseSeeder.seedCatalog(database)
            } catch (e: Exception) {
                // A failure here must not block startup: the app is usable with an empty
                // catalog, and the next launch will retry.
                Log.e(TAG, "Failed to seed exercise catalog", e)
            }
        }
    }

    /**
     * Replaces all workout history with a freshly generated demo set. Debug builds only —
     * gated by `BuildConfig.DEVELOPER_TOOLS` at the call site.
     *
     * Existing history is cleared first so that repeated presses are idempotent rather
     * than stacking duplicate workouts onto the same dates.
     */
    suspend fun regenerateDemoHistory() {
        mutex.withLock {
            database.workoutDao().deleteAllWorkouts()
            DatabaseSeeder.seedDemoHistory(database)
        }
    }

    /**
     * Deletes all logged workouts, keeping the exercise and template catalog. Debug builds
     * only.
     */
    suspend fun clearWorkoutHistory() {
        mutex.withLock {
            database.workoutDao().deleteAllWorkouts()
        }
    }

    private companion object {
        const val TAG = "DatabaseInitializer"

        /**
         * Instrumentation tests manage their own fixtures via `DatabaseSeedingRule`, and
         * assert against a known starting state. Auto-seeding would fight with that.
         */
        fun isInTestEnvironment(): Boolean = try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}
