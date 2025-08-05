package com.android.timberworkoutlogs.fixtures

import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import java.util.UUID


fun squatExerciseFixture(): ExerciseDefinition {
    val definitionId: UUID = UUID.randomUUID()
    return ExerciseDefinition(
        id = definitionId,
        name = "Squat",
        logType = LogType.WEIGHT_AND_REPS,
        muscleGroups = listOf(MuscleGroup.LEGS),
        equipment = ExerciseEquipment.BARBELL
    )
}
