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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectExerciseScreen(
    viewModel: SelectExerciseViewModel,
    onExerciseSelected: (UUID) -> Unit,
    onNavigateToCreateExercise: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val exercises by viewModel.filteredExercises.collectAsState()
    val searchText by viewModel.searchText.collectAsState()

    ContextualScaffold(
        title = {
            OutlinedTextField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
        },
        onNavigateBack = onNavigateBack,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateExercise) {
                Icon(Icons.Filled.Add, contentDescription = "Create new exercise definition")
            }
        }
    ) { innerPadding ->
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No exercises found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
