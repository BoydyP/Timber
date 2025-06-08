package com.bignerdranch.android.timberworkoutlogs.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreenContent(name = "Stats Screen", modifier = modifier)
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    PlaceholderScreenContent(name = "History Screen", modifier = modifier)
}

@Composable
fun TemplatesScreen(modifier: Modifier = Modifier) {
    PlaceholderScreenContent(name = "Templates Screen", modifier = modifier)
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreenContent(name = "Settings Screen", modifier = modifier)
}

@Composable
private fun PlaceholderScreenContent(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name)
    }
}
