package com.android.timberworkoutlogs.ui.screen.workout.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.WorkoutHistoryDisplayItem
import java.text.DecimalFormat

@Composable
fun WorkoutItemCard(
    displayItem: WorkoutHistoryDisplayItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = displayItem.workout.name, // The auto-generated name
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Performed on: ${displayItem.formattedStartTime}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(
                    text = "Exercises: ${displayItem.exerciseCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (displayItem.totalWeightLifted > 0) {
                    val df = DecimalFormat("#,###.##")
                    Text(
                        text = "Total Volume: ${df.format(displayItem.totalWeightLifted)} kg",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
