package com.example.healthanalytics

import android.app.Service
import android.content.Intent
import android.os.IBinder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

class AlarmTime : BroadcastReceiver() {

    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null


    override fun onReceive(context: Context, intent: Intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        editor = sharedPreferences!!.edit()
        editor!!.putString(context.getString(R.string.minimum), "")
        editor!!.commit()
        editor!!.putString(context.getString(R.string.maximum), "")
        editor!!.commit()
        editor!!.putString(context.getString(R.string.average), "")
        editor!!.commit()
    }


}

