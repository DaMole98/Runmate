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

import android.os.Debug
import android.os.IBinder
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.runmate.utils.CloudDBSingleton
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CaloriesService : Service(), SensorEventListener {

    // Binder given to clients.
    private val binder = LocalBinder()

    private var DB = CloudDBSingleton.getInstance()


    inner class LocalBinder : Binder() {

        // Return this instance of LocalService so clients can call public methods.
        fun getService(): CaloriesService = this@CaloriesService
    }

    private var sensorManager: SensorManager? = null

    private var isFirstStep = true
    var isTrainingPaused = false
    private var isComputing = false
    private var job: Job? = null

    // timestamps to handle calories computation
    private val computingTime = 3000
    private var start: Long = 0
    private var end: Long = 0

    // constants for calories computation
    // TODO(get these values from the database)
    private var horizontalComponent = 0.1
    private var verticalComponent = 1.8
    //private val sex = "Male" // "Female"
    private val h = 180 // [cm]
    private val m = 80f // [kg]
    private val G = 0.01f
    private val stepSize = (0.415 * h / 100).toFloat() // [m] // TODO(men = 0.415, women = 0.413, to be chosen based on the gender of the user)

    // variables to save steps, distance and calories
    private var currentSteps = 0
    private var totalSteps = 0
    private var totalDistance = 0
    private var totalCalories = 0f

    private lateinit var startTime: LocalTime

    lateinit var trainingType: String



    //traccia del servizio (misura il tempo di attività del servizio)
    private lateinit var serviceTrace : Trace

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //registerReceiver(shutdownReceiver, IntentFilter("STOP_SERVICE"))
        //registerReceiver(shutdownReceiver, IntentFilter("TRAINING_PAUSED"))

        serviceTrace = FirebasePerformance.getInstance().newTrace("CaloriesServiceTrace")
    }

    private fun initialize(){
        isFirstStep = true
        isTrainingPaused = false
        isComputing = false
        job = null
        start = 0
        end = 0
        currentSteps = 0
        totalSteps = 0
        totalDistance = 0
        totalCalories = 0f
        startTime = LocalTime.now()

        if (trainingType == "Corsa"){
            horizontalComponent = 0.2
            verticalComponent = 0.9
        }

        // step sensor registration
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
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
        job?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        //detach()

        serviceTrace.stop()

    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

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
        initialize()

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

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Runmate sta registrando la tua attività")
            //.setContentText("Service is running in the foreground")
            .setSmallIcon(R.drawable.runmate_running_man)
            .build()
    }

    // Detects user steps
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isTrainingPaused) {
            currentSteps++

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
            job = CoroutineScope(Dispatchers.Default).launch {
                while (isActive) {
                    val currentTime = SystemClock.elapsedRealtime()

                    // if no steps are taken for computingTime milliseconds
                    if (currentTime - end >= computingTime && !isComputing) {
                        isComputing = true
                        computeStats()
                        isComputing = false

                        job?.cancel()
                        isFirstStep = true
                    }
                }
            }
        }
    }

    // Updates stats
    private fun computeStats(){
        val interval = end - start
        if (interval != 0L) {
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

            //TrainingFragment().updateUI(totalSteps, totalDistance, totalCalories)
        }
    }

    // Computes calories based on the time interval. Returns distance and calories.
    private fun computeCalories(interval_in_milliseconds: Long): Pair<Int, Float> {
        val interval_in_seconds = interval_in_milliseconds / 1000f // [s]
        val d = currentSteps * stepSize // [m]
        val S = (d / interval_in_seconds) * 60 // [m / min]
        val VO2 = 3.5 + (horizontalComponent * S) + (verticalComponent * S * G) // [mL / (kg * min)]
        var calories = ((VO2 * m) / 1000) * 5 // [kcal / min]
        calories = calories * interval_in_seconds / 60 // [kcal]

        return Pair(d.toInt(), calories.toFloat())
    }

    // Locally saves stats
    private fun saveStats(){
        val sharedPref = getSharedPreferences("TRAINING_DATA", Context.MODE_PRIVATE)
        sharedPref?.edit()?.apply {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            //val duration = Duration.between(startTime, LocalTime.now())
            val trainingObj = TrainingObject(trainingType, currentDate, startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                totalSteps, totalDistance, totalCalories, TrainingFragment.elapsedFormatted)
            //totalSteps, totalDistance, totalCalories, "${duration.toHours()} h ${duration.toMinutes() % 60} min")

            // take older trainings (if any) and add last training
            var json = sharedPref.getString("trainingList", null)
            val gson = Gson()
            if (json != null) {
                val listType = object : TypeToken<MutableList<TrainingObject>>() {}.type
                val pastTraining = gson.fromJson<MutableList<TrainingObject>>(json, listType)
                pastTraining.add(trainingObj)
                json = gson.toJson(pastTraining)
                putString("trainingList", json)
            }
            else{ // last training is the first training
                val trainingList = mutableListOf(trainingObj)
                json = gson.toJson(trainingList)
            }
            putString("trainingList", json)
            putString("currentDate", currentDate)

            // old values are added to new values
            putInt("totalSteps", totalSteps + sharedPref.getInt("totalSteps", 0))
            putInt("totalDistance", totalDistance + sharedPref.getInt("totalDistance", 0))
            putFloat("totalCalories", totalCalories + sharedPref.getFloat("totalCalories", 0f))
            apply()

            val cloudData = HashMap<String, Any>()
            cloudData["trainingList"] = json
            cloudData["totalSteps"] = totalSteps + sharedPref.getInt("totalSteps", 0)
            cloudData["totalDistance"] = totalDistance + sharedPref.getInt("totalDistance", 0)
            cloudData["totalCalories"] = totalCalories + sharedPref.getFloat("totalCalories", 0f)
            cloudData["currentDate"] = currentDate

            val userId = FirebaseAuth.getInstance().currentUser.toString()

            val userRef = DB.getDBref().getReference("users").child(userId ?: "")

            //val database = (application as Runmate).database //ottieni l'istanza (singleton) del database dalla classe applicaiton
            //val databaseRef = database.reference
           // val usersRef = databaseRef.child("users").child(uid)



        }
    }

    /*private val shutdownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            // stop the service
            if (intent?.action == "STOP_SERVICE") {
                if (currentSteps != 0 && currentSteps != 1 && totalSteps != currentSteps) {
                    computeStats()
                }
                if (totalSteps != 0)
                    saveStats()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else if (intent?.action == "TRAINING_PAUSED")
                isTrainingPaused = !isTrainingPaused
        }
    }*/

    // Stops the service
    fun registerTraining() {
        if (currentSteps != 0 && currentSteps != 1){
            if (!isComputing) {
                isComputing = true
                computeStats()
                isComputing = false
            }
        }
        if(totalSteps != 0) {
            saveStats()
            Toast.makeText(baseContext, "Attività registrata!", Toast.LENGTH_SHORT).show()
        }

        sensorManager?.unregisterListener(this)
        job?.cancel()
        //detach()
        /*if (currentSteps != 0 && currentSteps != 1 && totalSteps != currentSteps)
            computeStats()
        if (totalSteps != 0) {
            saveStats()
            totalSteps = 0
        }*/

    }

    /*private fun detach(){
        sensorManager?.unregisterListener(this)
        job?.cancel()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }*/
}