import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.ui.navigation.AppDestinations

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    val route: String
)

private val BottomBarDefaultItems = listOf(
    // The "Stats" item now correctly points to the STATS_ROUTE
    BottomNavItem("Stats", Icons.Outlined.BarChart, "Statistics and analytics", AppDestinations.STATS_ROUTE),
    BottomNavItem("History", Icons.Outlined.History, "Workout history", AppDestinations.HISTORY_ROUTE),
    BottomNavItem("Workout", Icons.Outlined.FitnessCenter, "Workout now", AppDestinations.WORKOUT_ROUTE),
    BottomNavItem("Templates", Icons.Outlined.AddBox, "Workout templates", AppDestinations.TEMPLATES_ROUTE),
    BottomNavItem("Settings", Icons.Outlined.Settings, "Settings", AppDestinations.SETTINGS_ROUTE)
)

@Composable
fun TimberBottomNavigationBar(
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = BottomBarDefaultItems,
) {
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val isSelected = if (item.route != AppDestinations.WORKOUT_ROUTE) {
                currentRoute == item.route
            } else {
                false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                        modifier = if (item.label == "Workout") Modifier.size(32.dp) else Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                alwaysShowLabel = true
            )
        }
    }
}
