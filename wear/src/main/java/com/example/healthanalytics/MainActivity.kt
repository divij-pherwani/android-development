package com.example.healthanalytics

import android.Manifest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.widget.TextView

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.concurrent.ExecutionException


import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity


class MainActivity : WearableActivity(), SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mTextView: TextView? = null
    private var mSensorManager: SensorManager? = null
    private var mHeartRateSensor: Sensor? = null


    private var pendingIntent: PendingIntent? = null
    private lateinit var apiClient: GoogleApiClient
    private var activityRecognition: ActivityRecognitionClient? = null

    private var alarmManager: AlarmManager? = null
    private var alarmIntent: PendingIntent? = null


    private lateinit  var msg: String
    internal var hrValue = ArrayList<Int>()
    internal var datapath = "/my_path"
    internal var max: Int = 0
    internal var min: Int = 0
    internal var avg: Int = 0
    private var minView: TextView? = null
    private var maxView: TextView? = null
    private var avgView: TextView? = null
    private var activityView: TextView? = null
    private var mode: Int = 0

    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android" + "" + ".permission.BODY_SENSORS") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf("android.permission" + ".BODY_SENSORS"), 0)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission("android" + "" + ".permission.ACTIVITY_RECOGNITION") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf("android.permission" + ".ACTIVITY_RECOGNITION"), 0)
        }


        mTextView = findViewById(R.id.text)
        minView = findViewById(R.id.textMin)
        maxView = findViewById(R.id.textMax)
        avgView = findViewById(R.id.textAvg)
        activityView = findViewById(R.id.activityView)

        apiClient = GoogleApiClient.Builder(this@MainActivity).addApi(ActivityRecognition.API).addConnectionCallbacks(this@MainActivity).addOnConnectionFailedListener(this@MainActivity).build()
        apiClient.connect()
        activityRecognition = ActivityRecognitionClient(this)
        //activityRecognition.;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        editor = sharedPreferences!!.edit()
        checkSharedPreference()


        val messageFilter = IntentFilter(Intent.ACTION_SEND)
        val messageReceiver = Receiver()
        registerReceiver(messageReceiver, messageFilter)

        sensorData()
        // Enables Always-on
        setAmbientEnabled()
        alarm()
    }

    override fun onConnected(bundle: Bundle?) {
        val intent = Intent(this@MainActivity, ActivityRecognitionService::class.java)
        pendingIntent = PendingIntent.getService(this@MainActivity, 0, intent, 0)
        activityRecognition!!.requestActivityUpdates(0, pendingIntent)

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    inner class Receiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val msg = intent.getStringExtra("MY_KEY")
            activityView!!.text = msg
            SendMessage(datapath, "Activity:$msg").start()


        }
    }

    private fun checkSharedPreference()
    {

        val minimum: String = sharedPreferences!!.getString(getString(R.string.minimum), "")!!
        val average: String = sharedPreferences!!.getString(getString(R.string.average), "")!!
        val maximum: String = sharedPreferences!!.getString(getString(R.string.maximum), "")!!
        if (minimum.matches("".toRegex()) || average.matches("".toRegex()) || maximum.matches("".toRegex()) || minimum.matches("---".toRegex()) || average.matches("---".toRegex()) || maximum.matches("---".toRegex()))
        {

        }
        else
        {
            minView!!.text = minimum
            avgView!!.text = average
            maxView!!.text = maximum

            min = Integer.valueOf(minimum)
            avg = Integer.valueOf(average)
            max = Integer.valueOf(maximum)

            hrValue.add(Integer.valueOf(average))
        }
    }

    private fun saveSharedPreferences(min: String, avg: String, max: String)
    {
        editor!!.putString(getString(R.string.minimum), min)
        editor!!.commit()

        editor!!.putString(getString(R.string.average), avg)
        editor!!.commit()

        editor!!.putString(getString(R.string.maximum), max)
        editor!!.commit()
    }


    private fun alarm()
    {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 45)
        val intent = Intent(this, AlarmTime::class.java)
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (alarmManager != null) {
            alarmManager!!.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent)
        }

    }

    private fun sensorData() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mHeartRateSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
    }

    override fun onStop()
    {
        saveSharedPreferences(minView!!.text.toString(), avgView!!.text.toString(), maxView!!.text.toString())
        super.onStop()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun calculate() {
        if (min != 0) {
            if (Collections.min(hrValue) < min) {
                min = Collections.min(hrValue)
            }
        } else {
            min = Collections.min(hrValue)
        }
        if (max != 0) {
            if (Collections.max(hrValue) > max) {
                max = Collections.max(hrValue)
            }
        } else {
            max = Collections.max(hrValue)
        }
        val size = hrValue.size
        var sum = 0
        for (i in 0 until size) {
            sum = sum + hrValue[i]
        }
        avg = sum / size
        minView!!.text = min.toString()
        maxView!!.text = max.toString()
        avgView!!.text = avg.toString()
    }

    override fun onSensorChanged(event: SensorEvent) {

        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                msg = event.values[0].toInt().toString()
                mTextView!!.text = msg
                hrValue.add(Integer.valueOf(msg))
                calculate()
                SendMessage(datapath, msg).start()
                SendMessage(datapath, "Min:$min").start()
                SendMessage(datapath, "Max:$max").start()
                SendMessage(datapath, "Avg:$avg").start()
            }
        }
    }

    internal inner class SendMessage
    //Constructor for sending information to the Data Layer//

    (var path: String, var message: String) : Thread() {

        override fun run() {

            //Retrieve the connected devices//

            val nodeListTask = Wearable.getNodeClient(applicationContext).connectedNodes
            try {

                //Block on a task and get the result synchronously//

                val nodes = Tasks.await(nodeListTask)
                for (node in nodes) {

                    //Send the message///

                    val sendMessageTask = Wearable.getMessageClient(this@MainActivity).sendMessage(node.id, path, message.toByteArray())

                    try {

                        Tasks.await(sendMessageTask)

                        //Handle the errors//

                    } catch (exception: ExecutionException) {

                        //TO DO//

                    } catch (exception: InterruptedException) {

                        //TO DO//

                    }

                }

            } catch (exception: ExecutionException) {

                //TO DO//

            } catch (exception: InterruptedException) {

                //TO DO//

            }

        }
    }


}
