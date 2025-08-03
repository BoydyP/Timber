package com.android.timberworkoutlogs.ui.common

import android.content.Context
import android.os.Build
import android.widget.Toast

fun comingSoonToast(context: Context) {
    popToast(context, "Coming soon!")
}

fun timerFeatureNotSupportedToast(context: Context) {
    val currentBuild: Int = Build.VERSION.SDK_INT
    popToast(context, "Timer isn't available for Android API < 29. Your build is $currentBuild.")
}

private fun popToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
