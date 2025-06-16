package com.bignerdranch.android.timberworkoutlogs.ui.screen.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberOrange
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme

@Composable
fun CreateExerciseScreen(
    onExerciseCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    var exerciseName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create New Exercise")

        OutlinedTextField(
            value = exerciseName,
            onValueChange = { exerciseName = it },
            label = { Text("Exercise Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // TODO: Add fields for category, equipment, etc.

        Button(
            onClick = {
                // TODO: Save the new exercise to the database
                onExerciseCreated()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TimberOrange,
                contentColor = Color.Black
            )
        ) {
            Text("Save Exercise")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateExerciseScreenPreview() {
    TimberWorkoutLogsTheme {
        CreateExerciseScreen(onExerciseCreated = {})
    }
}
