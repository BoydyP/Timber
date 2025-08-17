package com.android.timberworkoutlogs.database.data

import com.android.timberworkoutlogs.models.TemplateExercise
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.ExerciseDefinition

object DefaultTemplates {

    fun getTemplatesWithExercises(defaultExercises: List<ExerciseDefinition>): Map<WorkoutTemplate, List<TemplateExercise>> {

        val squat = defaultExercises.first { it.name == "Squat" }
        val benchPress =
            defaultExercises.first { it.name == "Bench Press" && it.equipment.name.lowercase() == "barbell" }
        val deadlift = defaultExercises.first { it.name == "Deadlift" }
        val overheadPress =
            defaultExercises.first { it.name == "Overhead Press" && it.equipment.name.lowercase() == "barbell" }
        val pullUp = defaultExercises.first { it.name == "Pull Up" }
        val barbellRow = defaultExercises.first { it.name == "Bent Over Row" }
        val latPulldown = defaultExercises.first { it.name == "Lat Pulldown" }
        val dumbbellPress =
            defaultExercises.first { it.name == "Bench Press" && it.equipment.name.lowercase() == "dumbbell" }
        val bicepCurl =
            defaultExercises.first { it.name == "Bicep Curl" && it.equipment.name.lowercase() == "barbell" }
        val tricepPushdown = defaultExercises.first { it.name == "Tricep Pushdown" }
        val legPress = defaultExercises.first { it.name == "Leg Press" }
        val lunge =
            defaultExercises.first { it.name == "Lunge" && it.equipment.name.lowercase() == "dumbbell" }


        // The templateId is set to 0 as a placeholder. It will be replaced with the real ID during insertion.
        return mapOf(
            WorkoutTemplate(name = "Full Body Workout") to listOf(
                TemplateExercise(
                    templateId = 0,
                    definitionId = squat.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = benchPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = deadlift.id,
                    sets = listOf(WeightAndRepsSet(0.0, 5))
                )
            ),
            WorkoutTemplate(name = "Upper Body Workout") to listOf(
                TemplateExercise(
                    templateId = 0,
                    definitionId = benchPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 8),
                        WeightAndRepsSet(0.0, 8),
                        WeightAndRepsSet(0.0, 8)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = overheadPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 10),
                        WeightAndRepsSet(0.0, 10),
                        WeightAndRepsSet(0.0, 10)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = pullUp.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 0),
                        WeightAndRepsSet(0.0, 0),
                        WeightAndRepsSet(0.0, 0)
                    )
                )
            ),
            WorkoutTemplate(name = "StrongLifts 5x5 (Workout A)") to listOf(
                TemplateExercise(
                    templateId = 0,
                    definitionId = squat.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = benchPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = barbellRow.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5)
                    )
                )
            ),
            WorkoutTemplate(name = "StrongLifts 5x5 (Workout B)") to listOf(
                TemplateExercise(
                    templateId = 0,
                    definitionId = squat.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = overheadPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5),
                        WeightAndRepsSet(0.0, 5)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = deadlift.id,
                    sets = listOf(WeightAndRepsSet(0.0, 5))
                )
            ),
            WorkoutTemplate(name = "Push") to listOf(
                TemplateExercise(
                    templateId = 0,
                    definitionId = benchPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 8),
                        WeightAndRepsSet(0.0, 8),
                        WeightAndRepsSet(0.0, 8)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = dumbbellPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 12),
                        WeightAndRepsSet(0.0, 12),
                        WeightAndRepsSet(0.0, 12)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = overheadPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 10),
                        WeightAndRepsSet(0.0, 10),
                        WeightAndRepsSet(0.0, 10)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = tricepPushdown.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 15),
                        WeightAndRepsSet(0.0, 15),
                        WeightAndRepsSet(0.0, 15)
                    )
                )
            ),
            WorkoutTemplate(name = "Pull") to listOf(
                TemplateExercise(
                    templateId = 0,
                    definitionId = pullUp.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 0),
                        WeightAndRepsSet(0.0, 0),
                        WeightAndRepsSet(0.0, 0)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = barbellRow.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 8),
                        WeightAndRepsSet(0.0, 8),
                        WeightAndRepsSet(0.0, 8)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = latPulldown.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 12),
                        WeightAndRepsSet(0.0, 12),
                        WeightAndRepsSet(0.0, 12)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = bicepCurl.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 15),
                        WeightAndRepsSet(0.0, 15),
                        WeightAndRepsSet(0.0, 15)
                    )
                )
            ),
            WorkoutTemplate(name = "Legs") to listOf(
                TemplateExercise(
                    templateId = 0,
                    definitionId = squat.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 8),
                        WeightAndRepsSet(0.0, 8),
                        WeightAndRepsSet(0.0, 8)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = legPress.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 12),
                        WeightAndRepsSet(0.0, 12),
                        WeightAndRepsSet(0.0, 12)
                    )
                ),
                TemplateExercise(
                    templateId = 0,
                    definitionId = lunge.id,
                    sets = listOf(
                        WeightAndRepsSet(0.0, 15),
                        WeightAndRepsSet(0.0, 15),
                        WeightAndRepsSet(0.0, 15)
                    )
                )
            )
        )
    }
}
