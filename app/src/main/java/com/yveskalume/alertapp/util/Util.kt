package com.yveskalume.alertapp.util

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")


object PrefKey {
    val TRACKING = booleanPreferencesKey("tracking-enabled")
    val ALERTING = booleanPreferencesKey("alerting")
}

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
    val isRunning = manager?.getRunningServices(Int.MAX_VALUE)
        ?.any { it.service.className == serviceClass.name } ?: false

    if (isRunning) {
        return true
    }
    return false
}