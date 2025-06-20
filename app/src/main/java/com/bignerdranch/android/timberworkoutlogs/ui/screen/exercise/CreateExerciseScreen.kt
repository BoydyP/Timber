package com.bignerdranch.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseEquipment
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberOrange
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import com.bignerdranch.android.timberworkoutlogs.util.capitaliseEnum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    viewModel: CreateExerciseViewModel,
    onExerciseCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    var exerciseName by remember { mutableStateOf("") }
    var selectedEquipment by remember { mutableStateOf(ExerciseEquipment.BARBELL) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create New Exercise")

        OutlinedTextField(
            value = exerciseName,
            onValueChange = { exerciseName = it },
            label = { Text("Exercise Name") },
            placeholder = { Text("e.g. Bench Press") },
            modifier = Modifier.fillMaxWidth()
        )

        Box {
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = capitaliseEnum(selectedEquipment.name),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Equipment") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    ExerciseEquipment.entries.forEach { equipment ->
                        DropdownMenuItem(
                            text = { Text(capitaliseEnum(equipment.name)) },
                            onClick = {
                                selectedEquipment = equipment
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.saveExercise(exerciseName, selectedEquipment)
                onExerciseCreated()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TimberOrange,
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
        ) {
            Text("Save Exercise")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateExerciseScreenPreview() {
    TimberWorkoutLogsTheme {
        // TODO: Previewing this screen properly requires a fake ViewModel
        // CreateExerciseScreen(viewModel = ..., onExerciseCreated = {})
    }
}
