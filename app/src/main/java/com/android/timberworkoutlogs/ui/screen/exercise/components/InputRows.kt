package com.android.timberworkoutlogs.ui.screen.exercise.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.models.toStringResource

@Composable
fun WeightAndRepsInputRow(
    setNumber: Int,
    workoutSet: WeightAndRepsSet,
    unit: WeightUnit,
    onWeightChange: (Double) -> Unit,
    onRepsChange: (Int) -> Unit,
    onDoneChange: (Boolean) -> Unit,
    showIsDoneCheckbox: Boolean = true
) {
    var weightText by remember(workoutSet) { mutableStateOf(if (workoutSet.weight == 0.0) "" else workoutSet.weight.toString()) }
    var repsText by remember(workoutSet) { mutableStateOf(if (workoutSet.reps == 0) "" else workoutSet.reps.toString()) }

    Row(
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
            value = weightText,
            // Fix for bug BUG-013, invalid input on char '.'
            onValueChange = { newText ->
                if (newText.isEmpty() || newText.matches(Regex("""^\d*\.?\d*$"""))) {
                    weightText = newText
                    onWeightChange(newText.toDoubleOrNull() ?: 0.0)
                }
            },
            label = { Text("Weight (${stringResource(id = unit.toStringResource())})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = repsText,
            onValueChange = { newText ->
                if (newText.isEmpty() || newText.all { it.isDigit() }) {
                    repsText = newText
                    onRepsChange(newText.toIntOrNull() ?: 0)
                }
            },
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        if (showIsDoneCheckbox) {
            Checkbox(
                checked = workoutSet.isDone,
                onCheckedChange = onDoneChange,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .testTag("checkbox_$setNumber")
            )
        }
    }
}

@Composable
fun RepsOnlyInputRow(
    setNumber: Int,
    workoutSet: RepsOnlySet,
    onRepsChange: (Int) -> Unit,
    onDoneChange: (Boolean) -> Unit,
    showIsDoneCheckbox: Boolean = true
) {
    var repsText by remember(workoutSet) { mutableStateOf(if (workoutSet.reps == 0) "" else workoutSet.reps.toString()) }

    Row(
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
            value = repsText,
            onValueChange = { newText ->
                if (newText.isEmpty() || newText.all { it.isDigit() }) {
                    repsText = newText
                    onRepsChange(newText.toIntOrNull() ?: 0)
                }
            },
            label = { Text("Reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        if (showIsDoneCheckbox) {
            Checkbox(
                checked = workoutSet.isDone,
                onCheckedChange = onDoneChange,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun TimedInputRow(
    setNumber: Int,
    workoutSet: TimedSet,
    onDurationChange: (Int) -> Unit,
    onDoneChange: (Boolean) -> Unit,
    showIsDoneCheckbox: Boolean = true
) {
    var durationText by remember(workoutSet) { mutableStateOf(if (workoutSet.durationSeconds == 0) "" else workoutSet.durationSeconds.toString()) }

    Row(
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
            value = durationText,
            onValueChange = { newText ->
                if (newText.isEmpty() || newText.all { it.isDigit() }) {
                    durationText = newText
                    onDurationChange(newText.toIntOrNull() ?: 0)
                }
            },
            label = { Text("Duration") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            suffix = { Text("sec") }
        )
        if (showIsDoneCheckbox) {
            Checkbox(
                checked = workoutSet.isDone,
                onCheckedChange = onDoneChange,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun DistanceAndTimeInputRow(
    setNumber: Int,
    workoutSet: DistanceAndTimeSet,
    onDistanceChange: (Double) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDoneChange: (Boolean) -> Unit,
    showIsDoneCheckbox: Boolean = true
) {
    var distanceText by remember(workoutSet) { mutableStateOf(if (workoutSet.distance == 0.0) "" else workoutSet.distance.toString()) }
    var durationText by remember(workoutSet) { mutableStateOf(if (workoutSet.durationSeconds == 0) "" else workoutSet.durationSeconds.toString()) }

    Row(
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
            value = distanceText,
            onValueChange = { newText ->
                if (newText.isEmpty() || newText.matches(Regex("""^\d*\.?\d*$"""))) {
                    distanceText = newText
                    onDistanceChange(newText.toDoubleOrNull() ?: 0.0)
                }
            },
            label = { Text("Distance") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true,
            suffix = { Text("km") } // Assuming km, can be made dynamic later
        )
        OutlinedTextField(
            value = durationText,
            onValueChange = { newText ->
                if (newText.isEmpty() || newText.all { it.isDigit() }) {
                    durationText = newText
                    onDurationChange(newText.toIntOrNull() ?: 0)
                }
            },
            label = { Text("Duration") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            suffix = { Text("sec") }
        )
        if (showIsDoneCheckbox) {
            Checkbox(
                checked = workoutSet.isDone,
                onCheckedChange = onDoneChange,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
