import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimberTopAppBar(
    modifier: Modifier = Modifier,
    onIconClick: () -> Unit
) {
    val timberBackgroundColor = Color(0xFFf0aa49)

    TopAppBar(
        title = { Text("Timber", style = MaterialTheme.typography.headlineSmall) },
        actions = {
            IconButton(onClick = onIconClick) {
                Image(
                    painter = painterResource(id = R.drawable.timberlogo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
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
