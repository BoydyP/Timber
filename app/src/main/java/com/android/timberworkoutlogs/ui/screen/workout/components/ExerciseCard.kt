package com.android.timberworkoutlogs.ui.screen.workout.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.WorkoutExercise
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import java.util.UUID

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
                Text(
                    text = exerciseDefinition?.computedExerciseName ?: "Select Exercise...",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            workoutExercise.sets.forEachIndexed { index, set ->
                SetInputRow(
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "KG",
                        fontWeight = if (workoutExercise.unit == WeightUnit.KG) FontWeight.Bold else FontWeight.Normal
                    )
                    Switch(
                        checked = workoutExercise.unit == WeightUnit.LB,
                        onCheckedChange = { isLbs ->
                            onExerciseUnitChange(if (isLbs) WeightUnit.LB else WeightUnit.KG)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        "LB",
                        fontWeight = if (workoutExercise.unit == WeightUnit.LB) FontWeight.Bold else FontWeight.Normal
                    )
                }

                IconButton(onClick = onAddSet) {
                    Icon(
                        Icons.Filled.AddCircle,
                        contentDescription = "Add Set",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SetInputRow(
    setNumber: Int,
    workoutSet: ExerciseSet,
    unit: WeightUnit,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onDoneChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            value = if (workoutSet.weight == 0.0) "" else workoutSet.weight.toString(),
            onValueChange = onWeightChange,
            label = { Text("Weight") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        Checkbox(
            checked = workoutSet.isDone,
            onCheckedChange = onDoneChange,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExerciseCard() {
    TimberWorkoutLogsTheme {
        var workoutExercise by remember {
            mutableStateOf(
                WorkoutExercise(
                    id = UUID.randomUUID(),
                    workoutId = 1,
                    definitionId = UUID.randomUUID(),
                    unit = WeightUnit.KG,
                    sets = listOf(
                        ExerciseSet(weight = 100.0, reps = 8, isDone = true),
                        ExerciseSet(weight = 100.0, reps = 5)
                    )
                )
            )
        }
        val definition = ExerciseDefinition(
            id = workoutExercise.definitionId,
            name = "Bench Press",
            equipment = ExerciseEquipment.BARBELL,
            muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS)
        )

        ExerciseInputCard(
            exerciseDefinition = definition,
            workoutExercise = workoutExercise,
            onAddSet = {
                val newSets = workoutExercise.sets.toMutableList().apply { add(ExerciseSet()) }
                workoutExercise = workoutExercise.copy(sets = newSets)
            },
            onSetChanged = { index, updatedSet ->
                val newSets = workoutExercise.sets.toMutableList()
                newSets[index] = updatedSet
                workoutExercise = workoutExercise.copy(sets = newSets)
            },
            onExerciseUnitChange = { newUnit ->
                workoutExercise = workoutExercise.copy(unit = newUnit)
            },
            onNavigateToSelectExercise = {}
        )
    }
}
