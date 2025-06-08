package com.bignerdranch.android.timberworkoutlogs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.bignerdranch.android.timberworkoutlogs.ui.navigation.AppNavigation
import com.bignerdranch.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        lifecycleScope.launch {
            delay(1000L)
            keepSplashOnScreen = false
        }

        setContent {
            TimberWorkoutLogsTheme {
                // Set AppNavigation as the root composable
                AppNavigation()
            }
        }
    }
}
