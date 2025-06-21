package com.bignerdranch.android.timberworkoutlogs.ui.screen.exercise

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
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseDefinition
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseEquipment
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme

@Composable
fun ExercisesListScreen(
    viewModel: ExercisesListViewModel,
    onNavigateToCreateExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exercises by viewModel.allExercises.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateExercise) {
                Icon(Icons.Filled.Add, contentDescription = "Create new exercise")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Exercise Library",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No exercises defined yet. Tap the '+' to add one.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(exercises) { exercise ->
                        ExerciseDefinitionCard(exercise = exercise)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseDefinitionCard(
    exercise: ExerciseDefinition,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getIconForEquipment(exercise.equipment),
                contentDescription = exercise.equipment.name,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = exercise.computedExerciseName,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun getIconForEquipment(equipment: ExerciseEquipment): ImageVector {
    return when (equipment) {
        ExerciseEquipment.BARBELL -> Icons.Default.LineWeight
        ExerciseEquipment.DUMBBELL -> Icons.Default.FitnessCenter
        ExerciseEquipment.CABLE -> Icons.Default.Webhook
        ExerciseEquipment.MACHINE -> Icons.Default.SportsGymnastics
        ExerciseEquipment.BODYWEIGHT -> Icons.Default.SportsGymnastics
        ExerciseEquipment.KETTLEBELL -> Icons.Default.FitnessCenter
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview() {
    TimberWorkoutLogsTheme {
        // This preview won't show real data, just the empty state.
        // ExercisesListScreen(viewModel = ..., onNavigateToCreateExercise = {})
    }
}
