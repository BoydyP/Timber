package com.bignerdranch.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bignerdranch.android.timberworkoutlogs.models.ExerciseDefinition
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectExerciseScreen(
    viewModel: SelectExerciseViewModel,
    onExerciseSelected: (UUID) -> Unit,
    onNavigateToCreateExercise: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exercises by viewModel.filteredExercises.collectAsState()
    val searchText by viewModel.searchText.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchText,
                        onValueChange = viewModel::onSearchTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search exercises...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(exercises, key = { it.id }) { exercise ->
                    ExerciseListItem(
                        exercise = exercise,
                        onClick = { onExerciseSelected(exercise.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseListItem(
    exercise: ExerciseDefinition,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(exercise.computedExerciseName) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
fun SelectExerciseScreenPreview() {
    TimberWorkoutLogsTheme {
        // TODO: This preview will be basic as it doesn't have a real ViewModel
        // SelectExerciseScreen(...)
    }
}
