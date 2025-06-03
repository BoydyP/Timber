package com.bignerdranch.android.timberworkoutlogs.ui.screen

import TimberBottomNavigationBar
import TimberTopAppBar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme

// Data class to represent items in the Bottom Navigation Bar
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    // val route: String // Add this later for Jetpack Navigation
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // viewModel: HomeViewModel = hiltViewModel() // Example for later ViewModel integration
) {
    // State for the currently selected bottom navigation item
    // The "Workout Now" item (index 2) is selected by default.
    var selectedItemIndex by remember { mutableStateOf(2) }

    Scaffold(
        topBar = {
            TimberTopAppBar(onLanguageClick = { /* TODO: Handle language/region selection */ })
        },
        bottomBar = {
            TimberBottomNavigationBar(
                selectedItemIndex = selectedItemIndex,
                onItemSelected = { index ->
                    selectedItemIndex = index
                    // TODO: Handle navigation based on index or item.route
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        HomeScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}


@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp) // Add padding around the content
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Space between items
    ) {
        // Top Placeholder
        PlaceholderContent(
            label = "Top Placeholder Area",
            modifier = Modifier
                .weight(1f) // Takes up 1 part of the available vertical space
                .fillMaxWidth()
        )

        // Workouts this week section
        Column(
            modifier = Modifier
                .weight(1f) // Takes up 1 part of the available vertical space
                .fillMaxWidth()
        ) {
            Text(
                text = "Workouts this week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            PlaceholderContent(
                label = "KG / Day of Week Graph Placeholder",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize() // Fills the space allocated by the parent Column's weight
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
    TimberWorkoutLogsTheme { // Ensure this is your actual app theme
        HomeScreen()
    }
}
