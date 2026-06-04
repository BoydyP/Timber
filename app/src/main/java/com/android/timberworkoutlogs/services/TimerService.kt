package com.android.timberworkoutlogs.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.android.timberworkoutlogs.MainActivity
import com.android.timberworkoutlogs.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class TimerService : Service() {

    private val binder = TimerBinder()
    private var secondsElapsed = 0
    private var timerJob: Job? = null
    private val _timerText = MutableStateFlow("00:00")
    val timerText = _timerText.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val notificationChannelId = "TimerServiceChannel"

    private val _isTimerRunning = MutableStateFlow(false)

    val isTimerRunning = _isTimerRunning.asStateFlow()

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    fun startTimer() {
        createNotificationChannel()
        val notification = createNotification("00:00")
        // SPECIAL_USE is intentional — see manifest comment on TimerService.
        ServiceCompat.startForeground(
            this,
            1,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        timerJob?.cancel()
        _isTimerRunning.value = true
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                secondsElapsed++
                val newTime = formatTime(secondsElapsed)
                _timerText.value = newTime
                updateNotification(newTime)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun getSecondsElapsed(): Int {
        return secondsElapsed
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours,
            minutes,
            secs
        )
        else String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            notificationChannelId,
            "Timer Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(time: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Workout in progress")
            .setContentText("Timer: $time")
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(time: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(time)
        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        _isTimerRunning.value = false
        timerJob?.cancel()
    }
}
