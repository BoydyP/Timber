package com.android.timberworkoutlogs.ui.screen.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold
import com.android.timberworkoutlogs.ui.screen.templates.components.TemplateExerciseInputCard

@Composable
fun CreateTemplateScreen(
    viewModel: CreateTemplateViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSelectExercise: (exerciseIndex: Int) -> Unit
) {
    val templateName by viewModel.templateName
    val templateExercises by viewModel.templateExercises.collectAsState()
    val exerciseDefinitions by viewModel.exerciseDefinitions.collectAsState()

    ContextualScaffold(
        title = { Text(if (templateName.isEmpty()) "Create Template" else "Edit Template") },
        onNavigateBack = onNavigateBack,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.saveTemplate(onNavigateBack)
            }) {
                Icon(Icons.Filled.Done, contentDescription = "Save Template")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TextField(
                    value = templateName,
                    onValueChange = { viewModel.templateName.value = it },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            itemsIndexed(templateExercises) { index, exercise ->
                val definition = exerciseDefinitions[exercise.definitionId]
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
    }
}
