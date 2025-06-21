package com.bignerdranch.android.timberworkoutlogs.database.data

import com.bignerdranch.android.timberworkoutlogs.models.ExerciseDefinition
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseEquipment
import com.bignerdranch.android.timberworkoutlogs.models.MuscleGroup

object DefaultExercises {
    fun getPredefinedExercises(): List<ExerciseDefinition> {
        return listOf(
            ExerciseDefinition(name = "Bench Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)),
            ExerciseDefinition(name = "Bench Press", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)),
            ExerciseDefinition(name = "Incline Bench Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)),
            ExerciseDefinition(name = "Incline Bench Press", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS)),
            ExerciseDefinition(name = "Fly", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.CHEST)),
            ExerciseDefinition(name = "Fly", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.CHEST)),
            ExerciseDefinition(name = "Chest Press", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.CHEST)),
            ExerciseDefinition(name = "Push Up", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS)),
            ExerciseDefinition(name = "Dip", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS)),

            ExerciseDefinition(name = "Deadlift", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.LEGS, MuscleGroup.FULL_BODY)),
            ExerciseDefinition(name = "Bent Over Row", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "Bent Over Row", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "Pull Up", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "Lat Pulldown", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.BACK)),
            ExerciseDefinition(name = "Seated Row", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "T-Bar Row", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BACK)),
            ExerciseDefinition(name = "Pullover", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.CHEST)),

            ExerciseDefinition(name = "Squat", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.LEGS, MuscleGroup.FULL_BODY)),
            ExerciseDefinition(name = "Goblet Squat", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.LEGS)),
            ExerciseDefinition(name = "Goblet Squat", equipment = ExerciseEquipment.KETTLEBELL, muscleGroups = listOf(MuscleGroup.LEGS)),
            ExerciseDefinition(name = "Leg Press", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.LEGS)),
            ExerciseDefinition(name = "Lunge", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.LEGS)),
            ExerciseDefinition(name = "Lunge", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.LEGS)),
            ExerciseDefinition(name = "Romanian Deadlift", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.LEGS, MuscleGroup.BACK)),
            ExerciseDefinition(name = "Romanian Deadlift", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.LEGS, MuscleGroup.BACK)),
            ExerciseDefinition(name = "Leg Extension", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.LEGS)),
            ExerciseDefinition(name = "Leg Curl", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.LEGS)),
            ExerciseDefinition(name = "Calf Raise", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.LEGS)),
            ExerciseDefinition(name = "Calf Raise", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.LEGS)),

            ExerciseDefinition(name = "Overhead Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)),
            ExerciseDefinition(name = "Overhead Press", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)),
            ExerciseDefinition(name = "Lateral Raise", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS)),
            ExerciseDefinition(name = "Lateral Raise", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.SHOULDERS)),
            ExerciseDefinition(name = "Front Raise", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS)),
            ExerciseDefinition(name = "Shrug", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)),
            ExerciseDefinition(name = "Shrug", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)),
            ExerciseDefinition(name = "Face Pull", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK)),

            ExerciseDefinition(name = "Bicep Curl", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "Bicep Curl", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "Hammer Curl", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "Preacher Curl", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "Preacher Curl", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.BICEPS)),
            ExerciseDefinition(name = "Chin Up", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.BACK)),

            ExerciseDefinition(name = "Tricep Pushdown", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.TRICEPS)),
            ExerciseDefinition(name = "Skull Crusher", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.TRICEPS)),
            ExerciseDefinition(name = "Overhead Tricep Extension", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.TRICEPS)),
            ExerciseDefinition(name = "Close Grip Bench Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.CHEST))
        )
    }
}
