package com.yveskalume.alertapp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.yveskalume.alertapp.MainActivity
import com.yveskalume.alertapp.R
import com.yveskalume.alertapp.database.entities.Contact
import com.yveskalume.alertapp.ml.AudioClassifierHelper
import com.yveskalume.alertapp.util.PrefKey
import com.yveskalume.alertapp.util.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber


class TrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var classifier: AudioClassifierHelper
    private lateinit var locationCallback: LocationCallback
    private val classifierJob: Job = Job()

    private val contactDao by lazy {
        (application as com.yveskalume.alertapp.App).appContainer.contactDao
    }
    private val alertCategories =
        listOf("groan", "whimper", "crying", "sobbing", "screaming")


    override fun onCreate() {
        classifier = AudioClassifierHelper(
            context = applicationContext,
            onError = {
                Timber.e(it)
            },
            onResult = ::onClassificationResult
        )
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val activityIntent = Intent(this@TrackingService, MainActivity::class.java)
            .apply {
                this@apply.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val pendingIntent = PendingIntent.getActivity(
            this@TrackingService,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = Notification.Builder(
            this@TrackingService,
            getString(R.string.default_notification_channel_id)
        ).setContentTitle("Alertify")
            .setContentText("Is tracking your voice")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        startForeground(startId, notification)

        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit {
                it[PrefKey.TRACKING] = true
            }
            classifier.startAudioClassification()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun onClassificationResult(result: AudioClassifierResult) {
        val shouldAlert = result.classificationResults()
            .flatMap { it.classifications() }
            .flatMap { it.categories() }
            .flatMap { it.categoryName().split(",") }
            .any {
                Timber.e(it.lowercase())
                alertCategories.contains(it.lowercase())
            }

        if (shouldAlert) {
            CoroutineScope(Dispatchers.IO).launch {
                dataStore.edit {
                    it[PrefKey.ALERTING] = true
                }
                startAlerting()
            }
        }
    }

    private suspend fun startAlerting() {
        val contacts = getContacts()
        sendAlerts(contacts)
        classifier.stopAudioClassification()
    }


    private suspend fun getContacts(): List<Contact> {

        return contactDao.getAllContacts()
    }

    @SuppressLint("MissingPermission")
    private fun sendAlerts(contacts: List<Contact>) {
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.locations.forEach { location ->
                    Timber.e("Location: $location")
                    contacts.forEach { contact ->
                        val smsManager: SmsManager =
                            this@TrackingService.getSystemService(SmsManager::class.java)
                        smsManager.sendTextMessage(
                            contact.phone,
                            null,
                            "I'm in danger !! \nHere is my location: " +
                                    "https://maps.apple.com/?q=${location.latitude}," +
                                    "${location.longitude}",
                            null,
                            null
                        )
                    }
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(15000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        classifierJob.cancel()
        try {
            classifier.stopAudioClassification()
        } catch (e: Exception) {
            Timber.e(e)
        }
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit {
                it[PrefKey.ALERTING] = false
                it[PrefKey.TRACKING] = false
            }
        }
    }
}