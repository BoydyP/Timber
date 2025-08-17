package com.android.timberworkoutlogs.ui.screen.exercise

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.models.ExerciseEquipment
import com.android.timberworkoutlogs.models.LogType
import com.android.timberworkoutlogs.models.MuscleGroup
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold
import com.android.timberworkoutlogs.ui.screen.exercise.components.ExerciseDefinitionCard
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import java.util.UUID

@Composable
fun SelectExerciseScreen(
    viewModel: SelectExerciseViewModel,
    onExerciseSelected: (UUID) -> Unit,
    onNavigateToCreateExercise: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val exercises by viewModel.filteredExercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    ContextualScaffold(
        title = { Text("Select Exercise") },
        onNavigateBack = onNavigateBack,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateExercise) {
                Icon(Icons.Filled.Add, contentDescription = "Create new exercise definition")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val message = if (searchQuery.isNotBlank()) {
                        "No exercises match your search."
                    } else {
                        "No exercises defined yet. Tap the '+' to add one."
                    }
                    Text(message)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 80.dp // To avoid FAB overlap
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseDefinitionCard(
                            exercise = exercise,
                            modifier = Modifier.clickable { onExerciseSelected(exercise.id) }
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewSelectExerciseScreen() {
    TimberWorkoutLogsTheme {
        val exercise = ExerciseDefinition(
            id = UUID.randomUUID(),
            name = "Bench Press",
            equipment = ExerciseEquipment.BARBELL,
            logType = LogType.WEIGHT_AND_REPS,
            muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS)
        )
        Column {
            ExerciseDefinitionCard(exercise = exercise, modifier = Modifier.padding(16.dp))
        }
    }
}
