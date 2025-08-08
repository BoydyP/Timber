package com.android.timberworkoutlogs.ui.screen.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme

@Composable
fun TemplatesScreen(
    onNavigateToExercisesList: () -> Unit,
    onNavigateToWorkoutTemplatesList: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TemplateOptionCard(
            title = "Manage Exercises",
            description = "Create new exercises or edit your existing library.",
            onClick = onNavigateToExercisesList
        )
        TemplateOptionCard(
            title = "Workout Templates",
            description = "Create or edit pre-defined workout routines.",
            onClick = onNavigateToWorkoutTemplatesList
        )
    }
}

@Composable
private fun TemplateOptionCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TemplatesScreenPreview() {
    TimberWorkoutLogsTheme {
        TemplatesScreen(
            onNavigateToExercisesList = {},
            onNavigateToWorkoutTemplatesList = {}
        )
    }
}
