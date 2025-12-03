package com.android.timberworkoutlogs.ui.screen.stats.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.SecondaryEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.android.timberworkoutlogs.ui.screen.stats.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangePicker(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedTimeRange.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Time Range") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(SecondaryEditable)
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                TimeRange.entries.forEach { timeRange ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = timeRange.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (timeRange == selectedTimeRange) {
                                    FontWeight.Medium
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        },
                        onClick = {
                            onTimeRangeSelected(timeRange)
                            expanded = false
                        },
                        modifier = Modifier.testTag("time_range_${timeRange.displayName}")

                    )
                }
            }
        }
    }
}
