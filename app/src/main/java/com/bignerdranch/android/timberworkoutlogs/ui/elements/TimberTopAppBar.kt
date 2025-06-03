import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimberTopAppBar(
    modifier: Modifier = Modifier,
    onLanguageClick: () -> Unit
) {
    val timberBackgroundColor = Color(0xFFf0aa49)

    TopAppBar(
        title = { Text("Timber", style = MaterialTheme.typography.headlineSmall) },
        actions = {
            IconButton(onClick = onLanguageClick) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = "Select Language/Region"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = timberBackgroundColor,
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black
        ),
        modifier = modifier
    )
}