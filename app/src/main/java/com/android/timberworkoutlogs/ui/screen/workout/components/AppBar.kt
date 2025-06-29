package com.android.timberworkoutlogs.ui.screen.workout.components

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.ui.theme.WorkoutDiscard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopAppBar(
    title: String,
    timerText: String,
    onDiscardWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isConfirming by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(isConfirming) {
        if (isConfirming) {
            Toast.makeText(context, "Tap once more to confirm discard", Toast.LENGTH_SHORT).show()
            delay(2000)
            isConfirming = false
        }
    }

    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        actions = {
            Text(
                text = timerText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp)
            )
            IconButton(
                onClick = {
                    if (isConfirming) {
                        onDiscardWorkout()
                    } else {
                        isConfirming = true
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Discard workout",
                    tint = if (isConfirming) WorkoutDiscard else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = modifier
    )
}
