package com.example.runmate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CaloriesService : Service(), SensorEventListener {
    private var sensorManager: SensorManager? = null

    private var isFirstStep = true
    private var isTrainingPaused = false
    private var job: Job? = null

    // timestamps to handle calories computation
    private var start: Long = 0
    private var end: Long = 0

    // constants for calories computation
    // TODO(get these values from the database)
    private val h = 180 // [cm]
    private val m = 80f // [kg]
    private val G = 0.01f
    private val stepSize = (0.415 * h / 100).toFloat() // [m] // TODO(men = 0.415, women = 0.413, to be chosen based on the gender of the user)

    // variables to save steps, distance and calories
    private var currentSteps = 0
    private var totalSteps = 0
    private var totalDistance = 0
    private var totalCalories = 0f

    override fun onCreate() {
        super.onCreate()

        isFirstStep = true
        isTrainingPaused = false
        job = null

        currentSteps = 0
        totalSteps = 0
        totalDistance = 0
        totalCalories = 0f

        // step sensor registration
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        registerReceiver(shutdownReceiver, IntentFilter("STOP_SERVICE"))
        registerReceiver(shutdownReceiver, IntentFilter("TRAINING_PAUSED"))
    }

    // TEST
    private fun detectVirtualSteps(){
        currentSteps++

        if (isFirstStep) {
            start = SystemClock.elapsedRealtime()
            isFirstStep = false
        }
        else{
            end = SystemClock.elapsedRealtime()
            startCoroutineCalories()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager?.unregisterListener(this)
        unregisterReceiver(shutdownReceiver)
        job?.cancel()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // show a notification on the screen and allow the service to work in the background
        startForeground(1, createNotification())

        // TEST
        /*for (i in 1..10) {
            detectVirtualSteps()
            Thread.sleep(1000)
        }*/

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "RunmateCaloriesService"
        val channelName = "Runmate Activity Tracker"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Runmate sta registrando la tua attività")
            //.setContentText("Service is running in the foreground")
            .setSmallIcon(R.drawable.runmate_running_man)
            .build()
    }

    // Detects user steps
    override fun onSensorChanged(event: SensorEvent?) {
        currentSteps++

        if (!isTrainingPaused) {
            if (isFirstStep) {
                start = SystemClock.elapsedRealtime()
                isFirstStep = false
            } else {
                end = SystemClock.elapsedRealtime()
                startCoroutineCalories()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun startCoroutineCalories() {
        if (job == null || job?.isCancelled == true) {

            job = CoroutineScope(Dispatchers.Default).launch() {
                while (isActive) {
                    val currentTime = SystemClock.elapsedRealtime()

                    // if no steps are taken for 5 seconds
                    if (currentTime - end >= 5000) {
                        val interval = end - start
                        val (distance, calories) = computeCalories(interval)

                        totalSteps += currentSteps
                        totalDistance += distance
                        totalCalories += calories

                        currentSteps = 0

                        // send broadcast to the activity to update the UI
                        val intentUI = Intent("UPDATE_UI")

                        intentUI.putExtra("totalSteps", totalSteps)
                        intentUI.putExtra("totalDistance", totalDistance)
                        intentUI.putExtra("totalCalories", totalCalories)
                        sendBroadcast(intentUI)

                        job?.cancel()
                        isFirstStep = true
                    }
                }
            }
        }
    }

    // Computes calories based on the time interval. Returns distance and calories.
    private fun computeCalories(interval_in_milliseconds: Long): Pair<Int, Float> {
        val interval_in_seconds = interval_in_milliseconds / 1000f // [s]
        val d = currentSteps * stepSize // [m]
        val S = (d / interval_in_seconds) * 60 // [m / min]
        val VO2 = 3.5 + (0.1 * S) + (1.8 * S * G) // [mL / (kg * min)]
        var calories = ((VO2 * m) / 1000) * 5 // [kcal / min]
        calories = calories * interval_in_seconds / 60 // [kcal]

        return Pair(d.toInt(), calories.toFloat())
    }

    private val shutdownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            // stop the service
            if (intent?.action == "STOP_SERVICE") {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else if (intent?.action == "TRAINING_PAUSED")
                isTrainingPaused = !isTrainingPaused
        }
    }
}