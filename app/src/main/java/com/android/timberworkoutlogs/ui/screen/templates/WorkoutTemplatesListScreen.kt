package com.android.timberworkoutlogs.ui.screen.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.WorkoutTemplate
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold
import java.util.UUID

@Composable
fun WorkoutTemplatesListScreen(
    viewModel: WorkoutTemplatesViewModel,
    onNavigateToCreateTemplate: () -> Unit,
    onNavigateToEditTemplate: (UUID) -> Unit,
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
                items(templates, key = { it.id }) { template ->
                    WorkoutTemplateCard(
                        template = template,
                        modifier = Modifier.clickable {
                            // The model uses Long for ID, but UUID is better for navigation safety
                            // This will need to be reconciled in the Create/Edit screen's ViewModel
                            // For now, we don't have a UUID on the template model.
                            // onNavigateToEditTemplate(template.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutTemplateCard(
    template: WorkoutTemplate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = template.name, style = MaterialTheme.typography.titleMedium)
            // Can add more details here later, like number of exercises
        }
    }
}
