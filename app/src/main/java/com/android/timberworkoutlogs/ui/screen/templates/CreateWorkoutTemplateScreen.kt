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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold
import com.android.timberworkoutlogs.ui.screen.templates.components.TemplateExerciseInputCard
import com.android.timberworkoutlogs.ui.theme.TimberOrange


@Composable
fun CreateTemplateScreen(
    viewModel: CreateWorkoutTemplateViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSelectExercise: (exerciseIndex: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val screenTitle = if (uiState.isEditing) "Edit Template" else "Create Template"

    ContextualScaffold(
        title = { Text(screenTitle) },
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    TemplateExerciseInputCard(
                        exerciseDefinition = definition,
                        templateExercise = exercise,
                        onAddSet = { /*TODO*/ },
                        onDeleteSet = { /*TODO*/ },
                        onSetChanged = { _, _ -> /*TODO*/ },
                        onNavigateToSelectExercise = { onNavigateToSelectExercise(index) }
                    )
                }
                item {
                    Button(onClick = { viewModel.addExercise() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
                        Text("Add Exercise")
                    }
                }
            }
            Button(
                onClick = { viewModel.saveTemplate(onSuccess = onNavigateBack) },
                enabled = uiState.name.isNotBlank() && !uiState.isSaving && uiState.templateExercises.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TimberOrange,
                    contentColor = Color.Black
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Exercise")
                }
            }
        }
    }
}
