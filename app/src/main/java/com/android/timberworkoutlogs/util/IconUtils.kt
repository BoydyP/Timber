package com.android.timberworkoutlogs.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.ui.icons.Barbell
import com.android.timberworkoutlogs.ui.icons.Cables
import com.android.timberworkoutlogs.ui.icons.Dumbbell
import com.android.timberworkoutlogs.ui.icons.Kettlebell
import com.android.timberworkoutlogs.ui.icons.Machine

fun getIconForEquipment(equipment: ExerciseEquipment): ImageVector {
    return when (equipment) {
        ExerciseEquipment.BARBELL -> Barbell
        ExerciseEquipment.DUMBBELL -> Dumbbell
        ExerciseEquipment.CABLE -> Cables
        ExerciseEquipment.MACHINE -> Machine
        ExerciseEquipment.BODYWEIGHT -> Icons.Default.SportsGymnastics
        ExerciseEquipment.KETTLEBELL -> Kettlebell
    }
}