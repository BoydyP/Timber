package com.android.timberworkoutlogs.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.timberworkoutlogs.ui.screen.workout.components.WorkoutInProgressBanner
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import com.android.timberworkoutlogs.util.getGreetingByTime

@Composable
fun HomeScreen(
    navigateToWorkout: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val isWorkoutInProgress by viewModel.isWorkoutInProgress.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreenContent(modifier = Modifier.fillMaxSize())

        if (isWorkoutInProgress) {
            WorkoutInProgressBanner(
                modifier = Modifier.align(Alignment.TopCenter),
                navigateToWorkout = navigateToWorkout
            )
        }
    }
}

@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = getGreetingByTime(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp),
        )

        PlaceholderContent(
            label = "Best Lifts (e.g., Bench Press)",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = "Volume this week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            PlaceholderContent(
                label = "KG / Day of Week Graph Placeholder",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun PlaceholderContent(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun HomeScreenPreview() {
    TimberWorkoutLogsTheme {
        HomeScreen(navigateToWorkout = {})
    }
}
