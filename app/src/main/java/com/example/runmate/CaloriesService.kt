package com.example.runmate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.Debug
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.locks.ReentrantLock


class CaloriesService : Service(), SensorEventListener {
    private var isFirstStep = true
    private var isTrainingPaused = false
    private var isComputing = false

    private var sensorManager: SensorManager? = null
    private val statsLock = ReentrantLock()
    private var job: Job? = null

    // timestamps to handle calories computation
    private val computingTime = 10000L

    // constants for calories computation
    private var horizontalComponent = 0.1
    private var verticalComponent = 1.8
    private var gender = "Male" // "Female"
    private var h = 180 // [cm]
    private var m = 80 // [kg]
    private val G = 0.01f
    private var k = 0.42 // walk = 0.42, run = 0.6    //0.415 // men = 0.415, women = 0.413
    private var stepSize = (k * h / 100).toFloat() // [m]

    // variables to save steps, distance and calories
    private var currentSteps = 0
    private var totalSteps = 0
    private var totalDistance = 0
    private var totalCalories = 0f

    private lateinit var startTime: LocalTime

    lateinit var trainingType: String

    //private var DB = CloudDBSingleton.getInstance()

    //traccia del servizio (misura il tempo di attività del servizio)
    private lateinit var serviceTrace : Trace

    // Binder given to clients
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {

        // Returns this instance of CaloriesService so clients can call public methods
        fun getService(): CaloriesService = this@CaloriesService
    }

    fun setIsTrainingPaused(paused: Boolean){
        isTrainingPaused = paused
    }

    override fun onCreate() {
        super.onCreate()

        serviceTrace = FirebasePerformance.getInstance().newTrace("CaloriesServiceTrace")

        serviceTrace.start()

        // Misura il tempo di CPU utilizzato dal servizio
        val cpuTime = Debug.threadCpuTimeNanos()

        // Ottieni le informazioni sulla memoria del servizio
        val memoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryInfo)

        // Calcola il consumo di RAM totale del servizio
        val totalPss = memoryInfo.totalPss


        // Registra le metriche di consumo di CPU e RAM nel trace del servizio
        serviceTrace?.putMetric("cpu_time", cpuTime)
        serviceTrace?.putMetric("total_pss", totalPss.toLong())

        // show a notification on the screen and allow the service to work in the background
        startForeground(1, createNotification())

        // initialize some variables
        initialize()
    }

    override fun onBind(intent: Intent): IBinder {
        trainingType = intent.getStringExtra("trainingType").toString()
        if (trainingType == "Corsa"){
            k = 0.6
            horizontalComponent = 0.2
            verticalComponent = 0.9
        }
        stepSize = (k * h / 100).toFloat()
        return binder
    }

    private fun initialize(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // step sensor registration
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        // get user data
        val sharedPref = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
        gender = sharedPref.getString("Gender", "Male").toString()
        h = sharedPref.getInt("Height", 180)
        m = sharedPref.getInt("Weight", 80)

        //if (gender == "Female") k = 0.413
        //stepSize = (k * h / 100).toFloat()

        startTime = LocalTime.now()
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager?.unregisterListener(this)
        job?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)

        serviceTrace.stop()
    }

    private fun createNotification(): Notification {
        val channelId = "RunmateCaloriesService"
        val channelName = "Runmate Activity Tracker"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Runmate sta registrando la tua attività")
            .setSmallIcon(R.drawable.runmate_notification_icon)
            .build()
    }

    // Detects user steps
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isTrainingPaused) {
            currentSteps++

            if (isFirstStep) {
                isFirstStep = false
                startCoroutineCalories()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun startCoroutineCalories() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(computingTime)

                try {
                    statsLock.lock()
                    if (currentSteps != 0) {
                        computeStats()
                    }
                    else {
                        job?.cancel()
                        isFirstStep = true
                    }
                } finally {
                    statsLock.unlock()
                }
            }
        }
    }

    // Updates stats
    private fun computeStats(){
        val (distance, calories) = computeCalories()

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
    }

    // Computes calories based on the time interval. Returns distance and calories.
    private fun computeCalories(): Pair<Int, Float> {
        val interval_in_seconds = computingTime / 1000f // [s]
        val d = currentSteps * stepSize // [m]
        val S = (d / interval_in_seconds) * 60 // [m / min]
        val VO2 = 3.5 + (horizontalComponent * S) + (verticalComponent * S * G) // [mL / (kg * min)]
        var calories = ((VO2 * m) / 1000) * 5 // [kcal / min]
        calories = calories * interval_in_seconds / 60 // [kcal]

        return Pair(d.toInt(), calories.toFloat())
    }

    // Locally saves stats
    private fun saveStats(){
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val sharedPref = getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)
        //val sharedPref = getSharedPreferences("TRAINING_DATA", Context.MODE_PRIVATE)
        sharedPref?.edit()?.apply {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            val trainingObj = TrainingObject(trainingType, currentDate, startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                totalSteps, totalDistance, totalCalories, TrainingFragment.elapsedFormatted)

            // take older trainings (if any) and add last training
            var json = sharedPref.getString("trainingList", null)
            val gson = Gson()
            if (json != null) {
                val listType = object : TypeToken<MutableList<TrainingObject>>() {}.type
                val pastTraining = gson.fromJson<MutableList<TrainingObject>>(json, listType)
                pastTraining.add(trainingObj)
                json = gson.toJson(pastTraining)
            }
            else{ // last training is the first training
                val trainingList = mutableListOf(trainingObj)
                json = gson.toJson(trainingList)
            }
            putString("trainingList", json)
            putString("currentDate", currentDate)

            val ts = totalSteps + sharedPref.getInt("totalSteps", 0)
            val td = totalDistance + sharedPref.getInt("totalDistance", 0)
            val tc = totalCalories + sharedPref.getFloat("totalCalories", 0f)
            // old values are added to new values
            putInt("totalSteps", ts)
            putInt("totalDistance", td)
            putFloat("totalCalories", tc)
            apply()

            //updateDatabase(trainingObj, ts, td, tc)
            //val cloudData = HashMap<String, Any>()
            //cloudData["trainingList"] = json
            //cloudData["totalSteps"] = totalSteps + sharedPref.getInt("totalSteps", 0)
            //cloudData["totalDistance"] = totalDistance + sharedPref.getInt("totalDistance", 0)
            //cloudData["totalCalories"] = totalCalories + sharedPref.getFloat("totalCalories", 0f)
            //cloudData["currentDate"] = SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(Date())
//
            //val userId = FirebaseAuth.getInstance().currentUser.toString()

            //val userRef = DB.getDBref().getReference("users").child(userId ?: "")

            //val database = (application as Runmate).database //ottieni l'istanza (singleton) del database dalla classe applicaiton
            //val databaseRef = database.reference
            // val usersRef = databaseRef.child("users").child(uid)
        }
    }

   /* private fun updateDatabase(trainingObj : TrainingObject, totSteps : Int, totDist: Int, totCal : Float){

        val userId = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val userRef = DB.getDBref().getReference("users").child(userId ?: "")
        val newTraining = userRef.child("traininglist").push()
        newTraining.setValue(trainingObj)

    } */

    // Stops the service
    fun registerTraining() {
        try {
            statsLock.lock()
            if (currentSteps != 0){
                isComputing = true
                computeStats()
                job?.cancel()
            }
            if(totalSteps != 0) {
                saveStats()
                Toast.makeText(baseContext, "Attività registrata!", Toast.LENGTH_SHORT).show()
            }
        } finally {
            statsLock.unlock()
        }

        sensorManager?.unregisterListener(this)
    }
}