package com.android.timberworkoutlogs.ui.screen.workout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.models.WorkoutHistoryDisplayItem
import java.text.DecimalFormat

@Composable
fun WorkoutHistoryItemCard(
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
                text = displayItem.workout.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = displayItem.formattedStartTime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkoutStat(
                    icon = Icons.Outlined.Timer,
                    label = "Duration",
                    value = displayItem.durationInHms
                )

                WorkoutStat(
                    icon = Icons.Outlined.FitnessCenter,
                    label = "Exercises",
                    value = displayItem.exerciseCount.toString()
                )

                if (displayItem.totalWeightLifted > 0) {
                    val df = DecimalFormat("#,###")
                    WorkoutStat(
                        icon = Icons.Outlined.Scale,
                        label = "Volume",
                        value = "${df.format(displayItem.totalWeightLifted)} kg"
                    )
                }

                if (displayItem.totalDistance > 0) {
                    val df = DecimalFormat("#.##")
                    WorkoutStat(
                        icon = Icons.Outlined.Map,
                        label = "Distance",
                        value = "${df.format(displayItem.totalDistance)} km"
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutStat(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
