package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.ui.theme.TimberOrange
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import com.android.timberworkoutlogs.util.capitaliseEnum
import com.android.timberworkoutlogs.util.spaceSeparateEnum

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateExerciseScreen(
    viewModel: CreateExerciseViewModel,
    onExerciseCreated: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    var exerciseName by remember { mutableStateOf("") }
    var selectedEquipment by remember { mutableStateOf(ExerciseEquipment.BARBELL) }
    var selectedLogType by remember { mutableStateOf(LogType.WEIGHT_AND_REPS) }
    val selectedMuscleGroups = remember { mutableStateListOf<MuscleGroup>() }

    var isEquipmentDropdownExpanded by remember { mutableStateOf(false) }
    var isLogTypeDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = exerciseName,
            onValueChange = { exerciseName = it },
            label = { Text("Exercise Name") },
            placeholder = { Text("e.g. Bench Press") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = isEquipmentDropdownExpanded,
            onExpandedChange = { isEquipmentDropdownExpanded = !isEquipmentDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = capitaliseEnum(selectedEquipment.name),
                onValueChange = {},
                readOnly = true,
                label = { Text("Equipment") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isEquipmentDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isEquipmentDropdownExpanded,
                onDismissRequest = { isEquipmentDropdownExpanded = false }
            ) {
                ExerciseEquipment.entries.forEach { equipment ->
                    DropdownMenuItem(
                        text = { Text(capitaliseEnum(equipment.name)) },
                        onClick = {
                            selectedEquipment = equipment
                            isEquipmentDropdownExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = isLogTypeDropdownExpanded,
            onExpandedChange = { isLogTypeDropdownExpanded = !isLogTypeDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = capitaliseEnum(spaceSeparateEnum(selectedLogType.name)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Log Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLogTypeDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isLogTypeDropdownExpanded,
                onDismissRequest = { isLogTypeDropdownExpanded = false }
            ) {
                LogType.entries.forEach { logType ->
                    DropdownMenuItem(
                        text = { Text(capitaliseEnum(spaceSeparateEnum(logType.name))) },
                        onClick = {
                            selectedLogType = logType
                            isLogTypeDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Text(
            "Muscle Groups",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MuscleGroup.entries.forEach { muscleGroup ->
                val isSelected = selectedMuscleGroups.contains(muscleGroup)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedMuscleGroups.remove(muscleGroup)
                        else selectedMuscleGroups.add(muscleGroup)
                    },
                    label = { Text(capitaliseEnum(spaceSeparateEnum(muscleGroup.name))) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.saveExercise(
                    exerciseName,
                    selectedEquipment,
                    selectedMuscleGroups.toList(),
                    selectedLogType
                )
                onExerciseCreated()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = exerciseName.isNotBlank() && selectedMuscleGroups.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TimberOrange,
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text("Save Exercise")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateExerciseScreenPreview() {
    TimberWorkoutLogsTheme {
        // CreateExerciseScreen(viewModel = ..., onExerciseCreated = {})
    }
}
