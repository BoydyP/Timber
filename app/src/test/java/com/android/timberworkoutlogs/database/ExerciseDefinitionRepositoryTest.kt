package com.android.timberworkoutlogs.database

import com.android.timberworkoutlogs.fixtures.squatExerciseFixture
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ExerciseDefinitionRepositoryTest {

    private lateinit var dao: ExerciseDefinitionDao
    private lateinit var repository: ExerciseDefinitionRepository

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        repository = ExerciseDefinitionRepository(dao)
    }

    @Test
    fun `update calls modifyExerciseDefinition so edits are persisted`() = runTest {
        // Given an exercise that already exists in the database (same primary key).
        val exercise = squatExerciseFixture()

        // When updating it.
        repository.update(exercise)

        // Then the DAO's @Update method must be used, not @Insert(onConflict = IGNORE),
        // otherwise editing an existing row is silently dropped because the primary key
        // already exists.
        coVerify(exactly = 1) { dao.modifyExerciseDefinition(exercise) }
        coVerify(exactly = 0) { dao.addExerciseDefinition(exercise) }
    }
}
