package com.bignerdranch.android.timberworkoutlogs.ui.screen.workout.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseSet
import com.bignerdranch.android.timberworkoutlogs.models.WeightUnit
import com.bignerdranch.android.timberworkoutlogs.models.WorkoutExercise
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import java.util.UUID

@Composable
fun ExerciseInputCard(
    workoutExercise: WorkoutExercise,
    onAddSet: () -> Unit,
    onSetChanged: (setIndex: Int, updatedSet: ExerciseSet) -> Unit,
    onExerciseNameChange: (String) -> Unit,
    onExerciseUnitChange: (newUnit: WeightUnit) -> Unit, // New callback for unit change
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = workoutExercise.name,
                onValueChange = onExerciseNameChange,
                label = { Text("Exercise Name") },
                placeholder = { Text("e.g. Barbell Bench Press") },
                textStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            // The unit switch has been removed from here.

            workoutExercise.sets.forEachIndexed { index, set ->
                SetInputRow(
                    setNumber = index + 1,
                    workoutSet = set,
                    // Pass the unit down from the parent WorkoutExercise
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
                        text = "KG",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (workoutExercise.unit == WeightUnit.KG) FontWeight.Bold else FontWeight.Normal
                    )
                    Switch(
                        checked = workoutExercise.unit == WeightUnit.LB,
                        onCheckedChange = { isLbs ->
                            val newUnit = if (isLbs) WeightUnit.LB else WeightUnit.KG
                            onExerciseUnitChange(newUnit) // Call the new callback
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "LB",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (workoutExercise.unit == WeightUnit.LB) FontWeight.Bold else FontWeight.Normal
                    )
                }
                IconButton(
                    onClick = onAddSet,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
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
    unit: WeightUnit, // Receive the unit from the parent
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
        OutlinedTextField(
            value = if (workoutSet.weight == 0.0) "" else workoutSet.weight.toString(),
            onValueChange = onWeightChange,
            label = { Text("Weight") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            suffix = { Text(unit.name) } // Display the unit passed from the parent
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
        var exercise by remember {
            mutableStateOf(
                WorkoutExercise(
                    id = UUID.randomUUID(),
                    workoutId = 1,
                    name = "Barbell Bench Press",
                    unit = WeightUnit.KG,
                    sets = listOf(
                        ExerciseSet(weight = 100.0, reps = 8, isDone = true),
                        ExerciseSet(weight = 100.0, reps = 5)
                    )
                )
            )
        }
        ExerciseInputCard(
            workoutExercise = exercise,
            onAddSet = {
                val newSets = exercise.sets.toMutableList().apply { add(ExerciseSet()) }
                exercise = exercise.copy(sets = newSets)
            },
            onSetChanged = { index, updatedSet ->
                val newSets = exercise.sets.toMutableList()
                newSets[index] = updatedSet
                exercise = exercise.copy(sets = newSets)
            },
            onExerciseNameChange = { newName ->
                exercise = exercise.copy(name = newName)
            },
            onExerciseUnitChange = { newUnit ->
                exercise = exercise.copy(unit = newUnit)
            }
        )
    }
}
