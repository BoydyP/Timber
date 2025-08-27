package com.android.timberworkoutlogs.ui.screen.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.WeightUnit
import com.android.timberworkoutlogs.ui.screen.stats.TimeRange

@Composable
fun VolumeStatsTab(
    selectedTimeRange: TimeRange,
    weightUnit: WeightUnit,
    isLoading: Boolean,
    error: String?,
    onTimeRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Range Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimeRangePicker(
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = onTimeRangeSelected,
                modifier = Modifier.weight(1f)
            )
        }

        // Stats Section
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.error)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            else -> {
                // Enhanced Volume Statistics Placeholder
                // This will be expanded with actual volume data in future iterations
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Enhanced Volume Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "Coming Soon:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "• Volume trends over ${selectedTimeRange.displayName.lowercase()}\n" +
                                  "• Workout frequency analysis\n" +
                                  "• Muscle group distribution\n" +
                                  "• Training consistency metrics\n" +
                                  "• Personal records tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        val unitSymbol = if (weightUnit == WeightUnit.KG) "kg" else "lb"
                        Text(
                            text = "All data will be shown in $unitSymbol",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
