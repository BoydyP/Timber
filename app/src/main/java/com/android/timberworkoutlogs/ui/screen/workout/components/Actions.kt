package com.android.timberworkoutlogs.ui.screen.workout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.ui.theme.TimberTheme

@Composable
fun WorkoutBottomActions(
    onOpenNotes: () -> Unit,
    onFinishWorkout: () -> Unit,
    isFinishEnabled: Boolean,
    onOpenPlateCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenNotes, modifier = Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Session Notes", modifier = Modifier.size(32.dp))
        }
        CompleteWorkoutButton(onFinishWorkout = onFinishWorkout, isFinishEnabled = isFinishEnabled)
        IconButton(onClick = onOpenPlateCalculator, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.Calculate, contentDescription = "Plate Calculator", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun CompleteWorkoutButton(
    onFinishWorkout: () -> Unit,
    isFinishEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var isConfirming by remember { mutableStateOf(false) }

    val clickedColor = TimberTheme.customColors.success
    val containerColor = if (isConfirming) clickedColor else Color.Transparent
    val text = if (isConfirming) "Are you sure?" else "Complete workout"

    TextButton(
        onClick = {
            if (isConfirming) {
                onFinishWorkout()
            } else {
                isConfirming = true
            }
        },
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor
        ),
        modifier = modifier,
        enabled = isFinishEnabled
    ) {
        Text(text)
    }
}

@Preview
@Composable
private fun PreviewCompleteWorkoutButton() {
    CompleteWorkoutButton(
        onFinishWorkout = {},
        isFinishEnabled = true
    )
}