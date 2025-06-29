package com.android.timberworkoutlogs.ui.screen.workout.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.timberworkoutlogs.ui.theme.WorkoutDiscard

@Composable
fun AnimatedTrashIcon(
    isConfirming: Boolean,
    modifier: Modifier = Modifier
) {
    val lidOffset by animateDpAsState(
        targetValue = if (isConfirming) (-4).dp else 0.dp,
        label = "Lid Offset"
    )
    val lidRotation by animateFloatAsState(
        targetValue = if (isConfirming) -15f else 0f,
        label = "Lid Rotation"
    )
    val color = if (isConfirming) WorkoutDiscard else MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = TrashCanBase,
            contentDescription = "Discard workout",
            tint = color,
        )
        Icon(
            imageVector = TrashCanLid,
            contentDescription = null, // Lid is decorative
            tint = color,
            modifier = Modifier
                .offset(y = lidOffset)
                .rotate(lidRotation)
        )
    }
}

private val TrashCanBase: ImageVector
    get() {
        if (_trashCanBase != null) {
            return _trashCanBase!!
        }
        _trashCanBase = ImageVector.Builder(
            name = "TrashCanBase",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 4f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(6f, 19f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(8f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(7f)
            horizontalLineTo(6f)
            verticalLineToRelative(12f)
            close()
        }.build()
        return _trashCanBase!!
    }
private var _trashCanBase: ImageVector? = null


private val TrashCanLid: ImageVector
    get() {
        if (_trashCanLid != null) {
            return _trashCanLid!!
        }
        _trashCanLid = ImageVector.Builder(
            name = "TrashCanLid",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 4f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(19f, 4f)
            horizontalLineTo(15.5f)
            lineTo(14.5f, 3f)
            horizontalLineTo(9.5f)
            lineTo(8.5f, 4f)
            horizontalLineTo(5f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(14f)
            close()
        }.build()
        return _trashCanLid!!
    }
private var _trashCanLid: ImageVector? = null

@Preview(showBackground = true)
@Composable
private fun AnimatedTrashIconConfirmingPreview() {
    AnimatedTrashIcon(isConfirming = true)
}

@Preview(showBackground = true)
@Composable
private fun AnimatedTrashIconDefaultPreview() {
    AnimatedTrashIcon(isConfirming = false)
}
