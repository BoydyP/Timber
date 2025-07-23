package com.android.timberworkoutlogs.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.android.timberworkoutlogs.ui.common.ComingSoonToast

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreenContent(name = "Stats Screen", modifier = modifier)
}

@Composable
private fun PlaceholderScreenContent(
    name: String,
    modifier: Modifier = Modifier,
) {
    ComingSoonToast(LocalContext.current)
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name)
    }
}
