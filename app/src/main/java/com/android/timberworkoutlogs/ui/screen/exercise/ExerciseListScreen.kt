package com.android.timberworkoutlogs.ui.screen.exercise

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.ExerciseDefinition
import com.android.timberworkoutlogs.ui.screen.exercise.components.ExerciseDefinitionCard
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesListScreen(
    viewModel: ExercisesListViewModel,
    onNavigateToCreateExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exercises by viewModel.allExercises.collectAsState()
    var exerciseToDelete by remember { mutableStateOf<ExerciseDefinition?>(null) }
    val coroutineScope = rememberCoroutineScope()

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Delete Exercise") },
            text = { Text("Are you sure you want to delete ${exerciseToDelete!!.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExercise(exerciseToDelete!!)
                        exerciseToDelete = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Exercise Library") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateExercise) {
                Icon(Icons.Filled.Add, contentDescription = "Create new exercise")
            }
        }
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
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(exercises, key = { it.id }) { exercise ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                exerciseToDelete = exercise
                                return@rememberSwipeToDismissBoxState false
                            }
                            true
                        }
                    )

                    LaunchedEffect(exerciseToDelete) {
                        if (exerciseToDelete == null) {
                            coroutineScope.launch {
                                dismissState.reset()
                            }
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        backgroundContent = {
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                                    else -> Color.LightGray
                                }, label = "background color"
                            )
                            val scale by animateFloatAsState(
                                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1f else 0.75f,
                                label = "icon scale"
                            )

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color, shape = MaterialTheme.shapes.medium)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Icon",
                                    modifier = Modifier.scale(scale)
                                )
                            }
                        }
                    ) {
                        ExerciseDefinitionCard(
                            exercise = exercise,
                            modifier = Modifier
                        )
                    }
                }
            }
        }
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