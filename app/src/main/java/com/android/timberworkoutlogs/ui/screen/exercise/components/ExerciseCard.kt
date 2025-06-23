package com.android.timberworkoutlogs.ui.screen.exercise.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.util.getIconForEquipment

@Composable
fun ExerciseInputCard(
    exerciseDefinition: ExerciseDefinition?,
    workoutExercise: WorkoutExercise,
    onAddSet: () -> Unit,
    onSetChanged: (setIndex: Int, updatedSet: ExerciseSet) -> Unit,
    onExerciseUnitChange: (newUnit: WeightUnit) -> Unit,
    onNavigateToSelectExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToSelectExercise),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (exerciseDefinition != null) {
                        Icon(
                            imageVector = getIconForEquipment(exerciseDefinition.equipment),
                            contentDescription = "Equipment type",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = exerciseDefinition?.computedExerciseName ?: "Select Exercise...",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (exerciseDefinition != null) {
                workoutExercise.sets.forEachIndexed { index, set ->
                    when (set) {
                        is WeightAndRepsSet -> WeightAndRepsInputRow(
                            setNumber = index + 1,
                            workoutSet = set,
                            unit = workoutExercise.unit,
                            onWeightChange = { newWeightStr ->
                                val newWeight = newWeightStr.toDoubleOrNull() ?: 0.0
                                onSetChanged(index, set.copy(weight = newWeight))
                            },
                            onRepsChange = { newRepsStr ->
                                val newReps = newRepsStr.toIntOrNull() ?: 0
                                onSetChanged(index, set.copy(reps = newReps))
                            },
                            onDoneChange = { isDone ->
                                onSetChanged(index, set.copy(isDone = isDone))
                            }
                        )
                        is RepsOnlySet -> RepsOnlyInputRow(
                            setNumber = index + 1,
                            workoutSet = set,
                            onRepsChange = { newRepsStr ->
                                val newReps = newRepsStr.toIntOrNull() ?: 0
                                onSetChanged(index, set.copy(reps = newReps))
                            },
                            onDoneChange = { isDone ->
                                onSetChanged(index, set.copy(isDone = isDone))
                            }
                        )
                        is TimedSet -> TimedInputRow(
                            setNumber = index + 1,
                            workoutSet = set,
                            onDurationChange = { newDurationStr ->
                                val newDuration = newDurationStr.toIntOrNull() ?: 0
                                onSetChanged(index, set.copy(durationSeconds = newDuration))
                            },
                            onDoneChange = { isDone ->
                                onSetChanged(index, set.copy(isDone = isDone))
                            }
                        )
                        is DistanceAndTimeSet -> { /* TODO: Create a row for this type */ }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (exerciseDefinition?.logType == LogType.WEIGHT_AND_REPS) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("KG", fontWeight = if (workoutExercise.unit == WeightUnit.KG) FontWeight.Bold else FontWeight.Normal)
                        Switch(
                            checked = workoutExercise.unit == WeightUnit.LB,
                            onCheckedChange = { isLbs ->
                                onExerciseUnitChange(if (isLbs) WeightUnit.LB else WeightUnit.KG)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text("LB", fontWeight = if (workoutExercise.unit == WeightUnit.LB) FontWeight.Bold else FontWeight.Normal)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                IconButton(onClick = onAddSet, enabled = exerciseDefinition != null) {
                    Icon(
                        Icons.Filled.AddCircle,
                        contentDescription = "Add Set",
                        tint = if (exerciseDefinition != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightAndRepsInputRow(
    setNumber: Int,
    workoutSet: WeightAndRepsSet,
    unit: WeightUnit,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onDoneChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "$setNumber", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        OutlinedTextField(
            value = if (workoutSet.weight == 0.0) "" else workoutSet.weight.toString(),
            onValueChange = onWeightChange,
            label = { Text("Weight") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true,
            suffix = { Text(unit.name) }
        )
        OutlinedTextField(
            value = if (workoutSet.reps == 0) "" else workoutSet.reps.toString(),
            onValueChange = onRepsChange,
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Checkbox(checked = workoutSet.isDone, onCheckedChange = onDoneChange, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun RepsOnlyInputRow(
    setNumber: Int,
    workoutSet: RepsOnlySet,
    onRepsChange: (String) -> Unit,
    onDoneChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "$setNumber", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        OutlinedTextField(
            value = if (workoutSet.reps == 0) "" else workoutSet.reps.toString(),
            onValueChange = onRepsChange,
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Checkbox(checked = workoutSet.isDone, onCheckedChange = onDoneChange, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun TimedInputRow(
    setNumber: Int,
    workoutSet: TimedSet,
    onDurationChange: (String) -> Unit,
    onDoneChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "$setNumber", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        OutlinedTextField(
            value = if (workoutSet.durationSeconds == 0) "" else workoutSet.durationSeconds.toString(),
            onValueChange = onDurationChange,
            label = { Text("Duration") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            suffix = { Text("sec") }
        )
        Checkbox(checked = workoutSet.isDone, onCheckedChange = onDoneChange, modifier = Modifier.padding(start = 8.dp))
    }
}


//@Composable
//private fun SetInputRow(
//    setNumber: Int,
//    workoutSet: ExerciseSet,
//    unit: WeightUnit,
//    onWeightChange: (String) -> Unit,
//    onRepsChange: (String) -> Unit,
//    onDoneChange: (Boolean) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        Text(
//            text = "$setNumber",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.width(24.dp),
//            textAlign = TextAlign.Center
//        )
//        OutlinedTextField(
//            value = if (workoutSet. == 0.0) "" else workoutSet.weight.toString(),
//            onValueChange = onWeightChange,
//            label = { Text("Weight") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            modifier = Modifier.weight(1f),
//            singleLine = true,
//            suffix = { Text(unit.name) }
//        )
//        OutlinedTextField(
//            value = if (workoutSet.reps == 0) "" else workoutSet.reps.toString(),
//            onValueChange = onRepsChange,
//            label = { Text("Reps") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            modifier = Modifier.weight(1f),
//            singleLine = true
//        )
//        Checkbox(
//            checked = workoutSet.isDone,
//            onCheckedChange = onDoneChange,
//            modifier = Modifier.padding(start = 8.dp)
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Card with Exercise Selected")
//@Composable
//fun PreviewExerciseCard_Selected() {
//    TimberWorkoutLogsTheme {
//        var workoutExercise by remember {
//            mutableStateOf(
//                WorkoutExercise(
//                    id = UUID.randomUUID(),
//                    workoutId = 1,
//                    definitionId = UUID.randomUUID(),
//                    unit = WeightUnit.KG,
//                    sets = listOf(
//                        ExerciseSet(weight = 100.0, reps = 8, isDone = true),
//                        ExerciseSet(weight = 100.0, reps = 5)
//                    )
//                )
//            )
//        }
//        val definition = ExerciseDefinition(
//            id = workoutExercise.definitionId,
//            name = "Bench Press",
//            equipment = ExerciseEquipment.BARBELL,
//            muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS)
//        )
//
//        ExerciseInputCard(
//            exerciseDefinition = definition,
//            workoutExercise = workoutExercise,
//            onAddSet = {
//                val newSets = workoutExercise.sets.toMutableList().apply { add(ExerciseSet()) }
//                workoutExercise = workoutExercise.copy(sets = newSets)
//            },
//            onSetChanged = { index, updatedSet ->
//                val newSets = workoutExercise.sets.toMutableList()
//                newSets[index] = updatedSet
//                workoutExercise = workoutExercise.copy(sets = newSets)
//            },
//            onExerciseUnitChange = { newUnit ->
//                workoutExercise = workoutExercise.copy(unit = newUnit)
//            },
//            onNavigateToSelectExercise = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Card without Exercise Selected")
//@Composable
//fun PreviewExerciseCard_NotSelected() {
//    TimberWorkoutLogsTheme {
//        val workoutExercise by remember {
//            mutableStateOf(
//                WorkoutExercise(
//                    id = UUID.randomUUID(),
//                    workoutId = 1,
//                    definitionId = UUID.randomUUID(),
//                    unit = WeightUnit.KG,
//                    sets = listOf()
//                )
//            )
//        }
//
//        ExerciseInputCard(
//            exerciseDefinition = null,
//            workoutExercise = workoutExercise,
//            onAddSet = {},
//            onSetChanged = { _, _ -> },
//            onExerciseUnitChange = {},
//            onNavigateToSelectExercise = {}
//        )
//    }
//}
