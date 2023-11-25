package com.yveskalume.alertapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.datastore.preferences.core.edit
import com.yveskalume.alertapp.di.AppContainer
import com.yveskalume.alertapp.service.TrackingService
import com.yveskalume.alertapp.util.PrefKey
import com.yveskalume.alertapp.util.dataStore
import com.yveskalume.alertapp.util.isServiceRunning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.DebugTree


class App : Application() {

    val appContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        CoroutineScope(Dispatchers.IO).launch {
            val alerting = dataStore.data.first()[PrefKey.ALERTING]
            val tracking = dataStore.data.first()[PrefKey.TRACKING]

            if (alerting == true || tracking == true) {
                if (!isServiceRunning(TrackingService::class.java)) {
                    dataStore.edit {
                        it[PrefKey.ALERTING] = false
                        it[PrefKey.TRACKING] = false
                    }
                }
            }
        }
        Timber.plant(DebugTree())
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.default_notification_channel)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(
            getString(R.string.default_notification_channel_id),
            name,
            importance
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}