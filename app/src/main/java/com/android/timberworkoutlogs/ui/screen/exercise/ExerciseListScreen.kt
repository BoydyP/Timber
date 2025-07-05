package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.ui.components.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold
import com.android.timberworkoutlogs.ui.screen.exercise.components.ExerciseDefinitionCard
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import java.util.UUID

/**
 * This screen has been refactored to use the generic SwipeToDeleteContainer,
 * simplifying its code and removing duplicated logic.
 */
@Composable
fun ExerciseListScreen(
    viewModel: ExercisesListViewModel,
    onNavigateToEditExercise: (UUID) -> Unit,
    onNavigateBack: () -> Unit
) {
    val exercises by viewModel.allExercises.collectAsState()

    ContextualScaffold(
        title = { Text("Exercise Library") },
        onNavigateBack = onNavigateBack
    ) { innerPadding ->
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No exercises defined yet. Tap the '+' to add one.")
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
                items(exercises, key = { it.id }) { exercise ->
                    SwipeToDeleteContainer(
                        item = exercise,
                        onDismiss = {
                            viewModel.deleteExercise(it)
                        }
                    ) {
                        ExerciseDefinitionCard(
                            exercise = exercise,
                            modifier = Modifier.clickable { onNavigateToEditExercise(exercise.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExerciseListScreenPreview() {
    TimberWorkoutLogsTheme {
        // This preview will not show much as it's designed to be hosted in a Scaffold.
        // To create a meaningful preview, you would wrap it in a Scaffold here.
        // ExercisesListScreen(viewModel = ...)
    }
}
