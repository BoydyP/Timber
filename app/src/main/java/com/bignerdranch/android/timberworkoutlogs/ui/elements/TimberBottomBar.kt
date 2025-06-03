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
import com.bignerdranch.android.timberworkoutlogs.ui.screen.BottomNavItem

private val BottomBarDefaultItems = listOf(
        BottomNavItem("Stats", Icons.Outlined.BarChart, "Statistics and analytics"),
        BottomNavItem("History", Icons.Outlined.History, "Workout history"),
        BottomNavItem("Workout", Icons.Outlined.FitnessCenter, "Workout now"),
        BottomNavItem("Templates", Icons.Outlined.AddBox, "Add workout templates/exercises"), // Changed label for clarity
        BottomNavItem("Settings", Icons.Outlined.Settings, "Settings")
    )

@Composable
fun TimberBottomNavigationBar(
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = BottomBarDefaultItems,
    ) {
    NavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItemIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
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
