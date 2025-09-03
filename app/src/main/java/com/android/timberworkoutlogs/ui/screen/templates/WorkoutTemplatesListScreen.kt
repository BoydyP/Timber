package com.android.timberworkoutlogs.ui.screen.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.WorkoutTemplateWithExerciseCount
import com.android.timberworkoutlogs.ui.common.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold

@Composable
fun WorkoutTemplatesListScreen(
    viewModel: WorkoutTemplatesViewModel,
    onNavigateToCreateTemplate: () -> Unit,
    onNavigateToEditTemplate: (templateId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val templates by viewModel.templates.collectAsState()

    ContextualScaffold(
        title = { Text("Workout Templates") },
        onNavigateBack = onNavigateBack,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateTemplate) {
                Icon(Icons.Filled.Add, contentDescription = "Create Template")
            }
        }
    ) { innerPadding ->
        if (templates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No workout templates yet. Tap '+' to create one.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                items(templates, key = { it.workoutTemplate.id }) { templateItem ->
                    SwipeToDeleteContainer(
                        item = templateItem.workoutTemplate,
                        onDismiss = {
                            viewModel.deleteTemplate(it)
                        }
                    ) {
                        WorkoutTemplateCard(
                            template = templateItem,
                            modifier = Modifier.clickable {
                                onNavigateToEditTemplate(templateItem.workoutTemplate.id)
                            }
                                .testTag("WorkoutTemplateCard")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutTemplateCard(
    template: WorkoutTemplateWithExerciseCount,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "Exercise icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = template.workoutTemplate.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = getExerciseCountString(template.exerciseCount),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun getExerciseCountString(exerciseCount: Int): String {
    if (exerciseCount == 1) {
        return "$exerciseCount exercise"
    }
    return "$exerciseCount exercises"
    }