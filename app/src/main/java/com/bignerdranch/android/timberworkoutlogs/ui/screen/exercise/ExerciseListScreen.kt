package com.bignerdranch.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No exercises defined yet. Tap the '+' to add one.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(exercises) { exercise ->
                    ListItem(
                        headlineContent = { Text(text = exercise.computedExerciseName) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview() {
    TimberWorkoutLogsTheme {
        // Previewing this screen now requires a mocked ViewModel
        // ExercisesListScreen(viewModel = ..., onNavigateToCreateExercise = {})
    }
}
