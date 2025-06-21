package com.bignerdranch.android.timberworkoutlogs.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Barbell: ImageVector
    get() {
        if (_Barbell != null) {
            return _Barbell!!
        }
        _Barbell = ImageVector.Builder(
            name = "Barbell",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(12.7f, 28.9f)
                horizontalLineToRelative(6.6f)
                curveTo(22f, 29f, 22f, 29f, 23f, 30f)
                verticalLineToRelative(17f)
                horizontalLineToRelative(54f)
                lineTo(77f, 30f)
                curveToRelative(1.8f, -1.8f, 4.5f, -1.2f, 7f, -1.2f)
                horizontalLineToRelative(3.2f)
                curveTo(90f, 29f, 90f, 29f, 92f, 31f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(2.9f)
                curveToRelative(3.1f, 0f, 3.1f, 0f, 5.1f, 1f)
                verticalLineToRelative(28f)
                curveToRelative(-2.7f, 1.4f, -5f, 1f, -8f, 1f)
                lineToRelative(-0.4f, 2.4f)
                curveTo(91f, 70f, 91f, 70f, 90f, 71f)
                lineToRelative(-6f, 0.1f)
                horizontalLineToRelative(-3.3f)
                curveTo(78f, 71f, 78f, 71f, 77f, 70f)
                lineTo(77f, 53f)
                lineTo(23f, 53f)
                verticalLineToRelative(17f)
                curveToRelative(-1.8f, 1.8f, -4.5f, 1.2f, -7f, 1.2f)
                horizontalLineToRelative(-3.2f)
                curveTo(10f, 71f, 10f, 71f, 8f, 69f)
                verticalLineToRelative(-4f)
                lineTo(5.1f, 65f)
                curveTo(2f, 65f, 2f, 65f, 0f, 64f)
                lineTo(0f, 36f)
                curveToRelative(2.7f, -1.4f, 5f, -1f, 8f, -1f)
                lineToRelative(0.4f, -2.4f)
                curveToRelative(0.8f, -3.6f, 0.8f, -3.6f, 4.3f, -3.7f)
                close()
                moveTo(13f, 34f)
                lineToRelative(-1f, 5f)
                lineToRelative(-7f, 1f)
                verticalLineToRelative(20f)
                horizontalLineToRelative(3f)
                lineToRelative(1f, -16f)
                horizontalLineToRelative(3f)
                lineToRelative(1f, 22f)
                horizontalLineToRelative(5f)
                lineTo(18f, 34f)
                horizontalLineToRelative(-5f)
                close()
                moveTo(82f, 34f)
                verticalLineToRelative(32f)
                horizontalLineToRelative(5f)
                lineToRelative(1f, -5f)
                lineToRelative(7f, -1f)
                lineTo(95f, 40f)
                horizontalLineToRelative(-3f)
                lineToRelative(-1f, 16f)
                horizontalLineToRelative(-3f)
                lineToRelative(-1f, -22f)
                horizontalLineToRelative(-5f)
                close()
            }
        }.build()

        return _Barbell!!
    }

@Suppress("ObjectPropertyName")
private var _Barbell: ImageVector? = null
