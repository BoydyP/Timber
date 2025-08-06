package com.android.timberworkoutlogs.ui.screen.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.ui.common.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold
import com.android.timberworkoutlogs.ui.screen.templates.components.TemplateExerciseInputCard
import com.android.timberworkoutlogs.ui.theme.TimberOrange

@Composable
fun CreateTemplateScreen(
    viewModel: CreateWorkoutTemplateViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSelectExercise: (exerciseIndex: Int) -> Unit,
    onStartWorkout: (workoutId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val screenTitle = if (uiState.isEditing) "Edit Template" else "Create Template"

    ContextualScaffold(
        title = { Text(screenTitle) },
        onNavigateBack = onNavigateBack,
        actions = {
            if (uiState.isEditing) {
                IconButton(
                    onClick = { viewModel.startWorkout(onStartWorkout) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Workout")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChanged,
                        label = { Text("Template Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                itemsIndexed(uiState.templateExercises) { index, exercise ->
                    val definition = uiState.exerciseDefinitions[exercise.definitionId]
                    SwipeToDeleteContainer(
                        item = exercise,
                        onDismiss = { viewModel.removeExercise(index) }
                    ) {
                        TemplateExerciseInputCard(
                            exerciseDefinition = definition,
                            templateExercise = exercise,
                            weightUnit = uiState.weightUnit,
                            onAddSet = { viewModel.onAddSet(index) },
                            onDeleteSet = { setToDelete: ExerciseSet ->
                                val setIndex = exercise.sets.indexOf(setToDelete)
                                if (setIndex != -1) {
                                    viewModel.onDeleteSet(index, setIndex)
                                }
                            },
                            onSetChanged = { setIndex, newSet ->
                                viewModel.onSetChanged(
                                    index,
                                    setIndex,
                                    newSet
                                )
                            },
                            onNavigateToSelectExercise = { onNavigateToSelectExercise(index) }
                        )
                    }
                }
                item {
                    Button(onClick = {
                        val newExerciseIndex = uiState.templateExercises.size
                        viewModel.addExercise()
                        viewModel.onAddSet(newExerciseIndex)
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
                        Text("Add Exercise")
                    }
                }
                item {
                    Button(
                        onClick = { viewModel.saveTemplate(onSuccess = onNavigateBack) },
                        enabled = uiState.name.isNotBlank() && !uiState.isSaving && uiState.templateExercises.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TimberOrange,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Template")
                        }
                    }
                }
            }
        }
    }
}
