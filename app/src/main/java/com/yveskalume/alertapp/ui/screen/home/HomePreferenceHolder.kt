package com.yveskalume.alertapp.ui.screen.home

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.yveskalume.alertapp.util.PrefKey
import com.yveskalume.alertapp.util.dataStore
import kotlinx.coroutines.flow.map

@Composable
fun rememberHomePreferenceHolder(context: Context) = remember {
    HomePreferenceHolder(context)
}

@Stable
class HomePreferenceHolder(context: Context) {

    val alerting = context.dataStore.data.map { it[PrefKey.ALERTING] }
    val trackingEnabled = context.dataStore.data.map { it[PrefKey.TRACKING] }

}