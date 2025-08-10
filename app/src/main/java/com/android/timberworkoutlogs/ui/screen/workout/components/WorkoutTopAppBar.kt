package com.android.timberworkoutlogs.ui.screen.workout.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopAppBar(
    title: String,
    timerText: String,
    onDiscardWorkout: () -> Unit,
    onImportFromTemplate: () -> Unit,
    isConfirmingDiscard: Boolean,
    onConfirmDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = onImportFromTemplate) {
                Icon(Icons.Default.Add, contentDescription = "Import from Template")
            }
            Text(
                text = timerText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp)
            )
            IconButton(
                onClick = {
                    if (isConfirmingDiscard) {
                        onDiscardWorkout()
                    } else {
                        onConfirmDiscard()
                    }
                },
            ) {
                AnimatedTrashIcon(isConfirming = isConfirmingDiscard)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier
    )
}
