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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.models.WorkoutExercise
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseSet
import java.util.UUID

@Composable
fun ExerciseInputCard(
    workoutExercise: WorkoutExercise,
    onAddSet: () -> Unit,
    onSetChanged: (setIndex: Int, updatedSet: ExerciseSet) -> Unit,
    onExerciseNameChange: (String) -> Unit,
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                singleLine = true
            )

            workoutExercise.sets.forEachIndexed { index, set ->
                SetInputRow(
                    setNumber = index + 1,
                    workoutSet = set,
                    onWeightChange = { newWeightStr ->
                        val newWeight = newWeightStr.toIntOrNull() ?: set.weight
                        onSetChanged(index, set.copy(weight = newWeight))
                    },
                    onRepsChange = { newRepsStr ->
                        val newReps = newRepsStr.toIntOrNull() ?: set.reps
                        onSetChanged(index, set.copy(reps = newReps))
                    },
                    onDoneChange = { isDone ->
                        onSetChanged(index, set.copy(isDone = isDone))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            IconButton(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.End)
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

@Composable
private fun SetInputRow(
    setNumber: Int,
    workoutSet: ExerciseSet,
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
            value = if (workoutSet.weight == 0 && workoutSet.reps == 0) "" else workoutSet.weight.toString(),
            onValueChange = onWeightChange,
            label = { Text("Weight") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text("${workoutSet.unit}") }
        )
        OutlinedTextField(
            value = if (workoutSet.weight == 0 && workoutSet.reps == 0) "" else workoutSet.reps.toString(),
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TODO: Switch or toggle for weight unit
        Switch(
            checked = false,
            onCheckedChange = {},
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth()

        )
    }
}

@Preview
@Composable
fun PreviewExerciseCard(){
    ExerciseInputCard(
        workoutExercise = WorkoutExercise(
            id = UUID.randomUUID(),
            workoutId = 1,
            name = "Barbell bench press"
        ),
        onAddSet = {  },
        onSetChanged = { setIndex, updatedSet -> {} },
        onExerciseNameChange = { newName -> {} }
    )
}
