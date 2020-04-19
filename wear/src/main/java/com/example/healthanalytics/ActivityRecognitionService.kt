package com.example.healthanalytics

import android.app.IntentService
import android.content.Intent
import android.content.Context
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity


class ActivityRecognitionService : IntentService("ActivityRecognitionService") {


    override fun onHandleIntent(intent: Intent?) {
        val result = ActivityRecognitionResult.extractResult(intent)
        handleDetectedActivity(result.mostProbableActivity)

    }


    private fun handleDetectedActivity(probableActivity: DetectedActivity) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND

        when (probableActivity.type) {
            DetectedActivity.IN_VEHICLE -> intent.putExtra("MY_KEY", "Driving Vehicle")
            DetectedActivity.ON_BICYCLE -> intent.putExtra("MY_KEY", "Riding Bicycle")
            DetectedActivity.ON_FOOT -> intent.putExtra("MY_KEY", "On foot")
            DetectedActivity.STILL -> intent.putExtra("MY_KEY", "Still")
            DetectedActivity.TILTING -> intent.putExtra("MY_KEY", "Tilting")
            DetectedActivity.RUNNING -> intent.putExtra("MY_KEY", "Running")
            DetectedActivity.UNKNOWN -> intent.putExtra("MY_KEY", "Unknown")
        }
        sendBroadcast(intent)
    }
}
