package com.android.timberworkoutlogs.ui.screen.stats.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.SecondaryEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.android.timberworkoutlogs.database.ExerciseDefinitionWithCount
import com.android.timberworkoutlogs.models.ExerciseDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionDropdown(
    selectedExercise: ExerciseDefinition?,
    availableExercises: List<ExerciseDefinitionWithCount>,
    onExerciseSelected: (ExerciseDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedExercise?.computedExerciseName ?: "Select Exercise",
                onValueChange = {},
                readOnly = true,
                label = { Text("Exercise") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(SecondaryEditable)
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .exposedDropdownSize()
            ) {
                availableExercises.forEach { exerciseCount ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = buildString {
                                    append(exerciseCount.exerciseDefinition.computedExerciseName)
                                    append(" (")
                                    append(exerciseCount.workoutCount)
                                    append(" session")
                                    if (exerciseCount.workoutCount != 1) append("s")
                                    append(")")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (exerciseCount.exerciseDefinition.id == selectedExercise?.id) {
                                    FontWeight.Medium
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        },
                        onClick = {
                            onExerciseSelected(exerciseCount.exerciseDefinition)
                            expanded = false
                        }
                    )
                }

                if (availableExercises.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "No exercises found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { expanded = false }
                    )
                }
            }
        }
    }
}
