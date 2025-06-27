package com.android.timberworkoutlogs.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    PlaceholderScreenContent(name = "Stats Screen", modifier = modifier, onClick = {
        Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
    })
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    PlaceholderScreenContent(name = "History Screen", modifier = modifier, onClick = {
        Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
    })
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    PlaceholderScreenContent(name = "Settings Screen", modifier = modifier, onClick = {
        Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
    })
}

@Composable
private fun PlaceholderScreenContent(
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name)
    }
}
