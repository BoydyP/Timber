package com.bignerdranch.android.timberworkoutlogs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.bignerdranch.android.timberworkoutlogs.ui.screen.HomeScreen
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // You can keep this if you are managing edge-to-edge
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        // To be replaced with data loading logic.
        lifecycleScope.launchWhenCreated {
            delay(1000L) // Delay until we have data loading in
            keepSplashOnScreen = false
        }

        setContent {
            TimberWorkoutLogsTheme {
                HomeScreen()
            }
        }
    }
}
