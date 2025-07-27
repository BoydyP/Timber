package com.android.timberworkoutlogs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.android.timberworkoutlogs.ui.navigation.TimberUi
import com.android.timberworkoutlogs.ui.screen.settings.SettingsViewModel
import com.android.timberworkoutlogs.ui.theme.TimberWorkoutLogsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

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
            val useDynamicTheme by settingsViewModel.dynamicTheme.collectAsState()
            TimberWorkoutLogsTheme(
                dynamicColor = useDynamicTheme
            ) {
                TimberUi()
            }
        }
    }
}
