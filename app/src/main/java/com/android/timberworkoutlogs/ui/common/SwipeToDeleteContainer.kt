package com.android.timberworkoutlogs.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * A generic, reusable composable that wraps any content to make it swipeable to delete.
 *
 * @param T The type of the item associated with the swipe action.
 * @param item The specific item that this container represents.
 * @param onDismiss A lambda function to be invoked when a swipe-to-dismiss is confirmed.
 *                  It receives the `item` as a parameter. The container will not dismiss
 *                  the item visually on its own; it delegates this to the caller, who
 *                  is responsible for removing the item from the state list.
 * @param modifier The modifier to be applied to the swipe container.
 * @param content The actual UI content to be displayed and made swipeable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDismiss: (T) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                showDialog = true
                return@rememberSwipeToDismissBoxState false // Prevent automatic dismissal
            }
            true
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                coroutineScope.launch { dismissState.reset() }
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDismiss(item)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        coroutineScope.launch { dismissState.reset() }
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val color by animateColorAsState(
                if (isSwiping) Color.Red.copy(alpha = 0.8f) else Color.Transparent,
                label = "background color"
            )
            val scale by animateFloatAsState(
                if (isSwiping) 1f else 0f,
                label = "icon scale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Icon",
                    modifier = Modifier.scale(scale),
                    tint = Color.White
                )
            }
        }
    ) {
        content()
    }
}
