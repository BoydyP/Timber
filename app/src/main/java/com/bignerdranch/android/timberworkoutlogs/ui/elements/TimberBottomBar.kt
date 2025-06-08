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
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.timberworkoutlogs.ui.navigation.AppDestinations
import com.bignerdranch.android.timberworkoutlogs.ui.screen.BottomNavItem

private val BottomBarDefaultItems = listOf(
    // Each item now has a route associated with it
    BottomNavItem("Stats", Icons.Outlined.BarChart, "Statistics and analytics", AppDestinations.HOME_ROUTE), // Assuming "Stats" is part of home for now
    BottomNavItem("History", Icons.Outlined.History, "Workout history", AppDestinations.HOME_ROUTE), // Assuming "History" is part of home for now
    BottomNavItem("Workout", Icons.Outlined.FitnessCenter, "Workout now", AppDestinations.NEW_WORKOUT_ROUTE),
    BottomNavItem("Templates", Icons.Outlined.AddBox, "Add workout templates/exercises", AppDestinations.HOME_ROUTE), // Assuming "Templates" is part of home for now
    BottomNavItem("Settings", Icons.Outlined.Settings, "Settings", AppDestinations.HOME_ROUTE) // Assuming "Settings" is part of home for now
)

@Composable
fun TimberBottomNavigationBar(
    selectedItemIndex: Int,
    onItemSelected: (Int, String) -> Unit, // Pass back index and route
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = BottomBarDefaultItems,
) {
    NavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItemIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(index, item.route) }, // Pass the route on click
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                        // Make the "Workout" icon larger
                        modifier = if (item.label == "Workout") Modifier.size(32.dp) else Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                alwaysShowLabel = true // Shows labels for all items
            )
        }
    }
}
