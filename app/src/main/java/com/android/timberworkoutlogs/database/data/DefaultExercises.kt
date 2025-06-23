package com.android.timberworkoutlogs.database.data

import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup

object DefaultExercises {
    fun getPredefinedExercises(): List<ExerciseDefinition> {
        return listOf(
            // --- CHEST ---
            ExerciseDefinition(name = "Bench Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Bench Press", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Incline Bench Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Incline Bench Press", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Fly", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.CHEST), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Fly", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.CHEST), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Chest Press", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.CHEST), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Push Up", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS), logType = LogType.REPS_ONLY),
            ExerciseDefinition(name = "Dip", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS), logType = LogType.REPS_ONLY),

            // --- BACK ---
            ExerciseDefinition(name = "Deadlift", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.LEGS, MuscleGroup.FULL_BODY), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Bent Over Row", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Bent Over Row", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Pull Up", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS), logType = LogType.REPS_ONLY),
            ExerciseDefinition(name = "Lat Pulldown", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.BACK), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Seated Row", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "T-Bar Row", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BACK), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Pullover", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BACK, MuscleGroup.CHEST), logType = LogType.WEIGHT_AND_REPS),

            // --- LEGS ---
            ExerciseDefinition(name = "Squat", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.LEGS, MuscleGroup.FULL_BODY), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Goblet Squat", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Goblet Squat", equipment = ExerciseEquipment.KETTLEBELL, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Leg Press", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Lunge", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Lunge", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.REPS_ONLY),
            ExerciseDefinition(name = "Romanian Deadlift", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.LEGS, MuscleGroup.BACK), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Romanian Deadlift", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.LEGS, MuscleGroup.BACK), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Leg Extension", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Leg Curl", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Calf Raise", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Calf Raise", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.LEGS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Swing", equipment = ExerciseEquipment.KETTLEBELL, muscleGroups = listOf(MuscleGroup.LEGS, MuscleGroup.BACK), logType = LogType.WEIGHT_AND_REPS),

            // --- SHOULDERS ---
            ExerciseDefinition(name = "Overhead Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Overhead Press", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Lateral Raise", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Lateral Raise", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.SHOULDERS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Front Raise", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Shrug", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Shrug", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Face Pull", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.SHOULDERS, MuscleGroup.BACK), logType = LogType.WEIGHT_AND_REPS),

            // --- BICEPS ---
            ExerciseDefinition(name = "Bicep Curl", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Bicep Curl", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Hammer Curl", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Preacher Curl", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Preacher Curl", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.BICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Chin Up", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.BACK), logType = LogType.REPS_ONLY),

            // --- TRICEPS ---
            ExerciseDefinition(name = "Tricep Pushdown", equipment = ExerciseEquipment.CABLE, muscleGroups = listOf(MuscleGroup.TRICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Skull Crusher", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.TRICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Overhead Tricep Extension", equipment = ExerciseEquipment.DUMBBELL, muscleGroups = listOf(MuscleGroup.TRICEPS), logType = LogType.WEIGHT_AND_REPS),
            ExerciseDefinition(name = "Close Grip Bench Press", equipment = ExerciseEquipment.BARBELL, muscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.CHEST), logType = LogType.WEIGHT_AND_REPS),

            // --- CARDIO & ABS ---
            ExerciseDefinition(name = "Treadmill Run", equipment = ExerciseEquipment.MACHINE, muscleGroups = listOf(MuscleGroup.FULL_BODY), logType = LogType.DISTANCE_AND_TIME),
            ExerciseDefinition(name = "Plank", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.ABS), logType = LogType.TIME),
            ExerciseDefinition(name = "Crunch", equipment = ExerciseEquipment.BODYWEIGHT, muscleGroups = listOf(MuscleGroup.ABS), logType = LogType.REPS_ONLY)
        )
    }
}
