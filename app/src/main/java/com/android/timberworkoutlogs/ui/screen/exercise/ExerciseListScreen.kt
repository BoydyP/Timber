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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.ui.common.SwipeToDeleteContainer
import com.android.timberworkoutlogs.ui.elements.ContextualScaffold
import com.android.timberworkoutlogs.ui.screen.exercise.components.ExerciseDefinitionCard
import java.util.UUID

const val TEST_TAG = "exercise_list"

@Composable
fun ExerciseListScreen(
    viewModel: ExercisesListViewModel,
    onNavigateToCreateExercise: () -> Unit,
    onNavigateToEditExercise: (UUID) -> Unit,
    onNavigateBack: () -> Unit
) {
    val exercises by viewModel.allExercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    ContextualScaffold(
        title = { Text("Exercise Library") },
        onNavigateBack = onNavigateBack,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateExercise) {
                Icon(Icons.Filled.Add, contentDescription = "Add Exercise")
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
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TEST_TAG)
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
}
