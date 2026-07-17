package com.android.timberworkoutlogs.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.timberworkoutlogs.models.DistanceAndTimeSet
import com.android.timberworkoutlogs.models.ExerciseSet
import com.android.timberworkoutlogs.models.RepsOnlySet
import com.android.timberworkoutlogs.models.TimedSet
import com.android.timberworkoutlogs.models.WeightAndRepsSet
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold
import com.android.timberworkoutlogs.util.getIconForEquipment
import java.util.Locale

@Composable
fun WorkoutHistoryDetailScreen(
    viewModel: WorkoutHistoryDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ContextualScaffold(
        title = {
            Text((uiState as? WorkoutHistoryDetailUiState.Success)?.displayItem?.workout?.name ?: "Workout")
        },
        onNavigateBack = onNavigateBack
    ) { innerPadding ->
        when (val state = uiState) {
            is WorkoutHistoryDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is WorkoutHistoryDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }

            is WorkoutHistoryDetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        WorkoutHistoryItemCard(displayItem = state.displayItem)
                    }
                    items(
                        items = state.exercises,
                        key = { it.exercise.id }
                    ) { exerciseWithDefinition ->
                        WorkoutHistoryExerciseCard(exerciseWithDefinition = exerciseWithDefinition)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryExerciseCard(
    exerciseWithDefinition: WorkoutExerciseWithDefinition,
    modifier: Modifier = Modifier
) {
    val definition = exerciseWithDefinition.definition
    val exercise = exerciseWithDefinition.exercise

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getIconForEquipment(definition.equipment),
                    contentDescription = "Equipment type",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = definition.computedExerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            exercise.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set ${index + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = describeSet(set, exercise.unit),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun describeSet(set: ExerciseSet, unit: WeightUnit): String {
    return when (set) {
        is WeightAndRepsSet -> {
            val unitLabel = if (unit == WeightUnit.LB) "lb" else "kg"
            "${set.reps} x ${formatNumber(set.weight)} $unitLabel"
        }
        is RepsOnlySet -> "${set.reps} reps"
        is TimedSet -> formatDuration(set.durationSeconds)
        is DistanceAndTimeSet ->
            "${formatNumber(set.distance)} km in ${formatDuration(set.durationSeconds)}"
    }
}

private fun formatNumber(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
}
