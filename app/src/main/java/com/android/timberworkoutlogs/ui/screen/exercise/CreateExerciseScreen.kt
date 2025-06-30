package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.util.capitaliseEnum
import com.android.timberworkoutlogs.util.spaceSeparateEnum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    viewModel: CreateExerciseViewModel,
    onExerciseCreated: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaving) {
        if (uiState.isSaving) {
            onExerciseCreated()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text("Exercise Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        EquipmentDropdown(
            selectedEquipment = uiState.equipment,
            onEquipmentSelected = viewModel::onEquipmentChanged
        )
        LogTypeDropdown(
            selectedLogType = uiState.logType,
            onLogTypeSelected = viewModel::onLogTypeChanged,
        )
        MuscleGroupSelector(
            selectedMuscleGroups = uiState.muscleGroups,
            onMuscleGroupsChanged = viewModel::onMuscleGroupsChanged
        )
        Button(
            onClick = viewModel::saveExercise,
            enabled = uiState.name.isNotBlank() && uiState.muscleGroups.isNotEmpty() && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Save Exercise")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentDropdown(
    selectedEquipment: ExerciseEquipment,
    onEquipmentSelected: (ExerciseEquipment) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = spaceSeparateEnum(capitaliseEnum(selectedEquipment.name)),
            onValueChange = {},
            readOnly = true,
            label = { Text("Equipment") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ExerciseEquipment.entries.forEach { equipment ->
                DropdownMenuItem(
                    text = {
                        Text(
                            spaceSeparateEnum(capitaliseEnum(equipment.name))
                        )
                    },
                    onClick = {
                        onEquipmentSelected(equipment)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MuscleGroupSelector(
    selectedMuscleGroups: List<MuscleGroup>,
    onMuscleGroupsChanged: (List<MuscleGroup>) -> Unit
) {
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

                    val newSelection = if (selectedMuscleGroups.contains(muscleGroup)) {
                        selectedMuscleGroups - muscleGroup
                    } else {
                        selectedMuscleGroups + muscleGroup
                    }
                    onMuscleGroupsChanged(newSelection)
                },
                label = { Text(capitaliseEnum(spaceSeparateEnum(muscleGroup.name))) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogTypeDropdown(
    selectedLogType: LogType,
    onLogTypeSelected: (LogType) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = spaceSeparateEnum(capitaliseEnum(selectedLogType.name)),
            onValueChange = {},
            readOnly = true,
            label = { Text("Log Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LogType.entries.forEach { logType ->
                DropdownMenuItem(
                    text = {
                        Text(
                            spaceSeparateEnum(capitaliseEnum(logType.name))
                        )
                    },
                    onClick = {
                        onLogTypeSelected(logType)
                        expanded = false
                    }
                )
            }
        }
    }
}
