package com.example.runmate

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import kotlin.math.roundToInt

class TrainingFragment:Fragment(R.layout.fragment_training) {
    private lateinit var cService: CaloriesService
    private lateinit var intentService: Intent
    private var isServiceBounded: Boolean = false

    private lateinit var tv_totalSteps: TextView
    private lateinit var tv_totalDistance: TextView
    private lateinit var tv_totalCalories: TextView
    private lateinit var chronometer: Chronometer

    private var isPlayed = false
    private var isPaused = false
    private var isServiceStarted = false
    private var pauseOffset: Long = 0

    private var steps = 0
    private var distance = 0
    private var calories = 0f

    private lateinit var trainingType: String

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object {
        var isTraining = false
        lateinit var elapsedFormatted: String
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {

            // We've bound to CaloriesService, cast the IBinder and get CaloriesService instance.
            val binder = service as CaloriesService.LocalBinder
            cService = binder.getService()
            isServiceBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBounded = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            trainingType = it.getString("trainingType").toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_training, container, false)

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        val tv_training_type = view.findViewById<TextView>(R.id.tv_training_type)
        tv_training_type.text = trainingType
        tv_totalSteps = view.findViewById(R.id.tv_steps_train)
        tv_totalDistance = view.findViewById(R.id.tv_distance_train)
        tv_totalCalories = view.findViewById(R.id.tv_calories_train)
        chronometer = view.findViewById(R.id.chronometer_train)

        chronometer.setOnChronometerTickListener {
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
            val elapsedSeconds = elapsedMillis / 1000
            val h = elapsedSeconds / 3600
            val m = (elapsedSeconds % 3600) / 60
            val s = elapsedSeconds % 60
            elapsedFormatted = "$h h $m min"
            chronometer.text = String.format("%02d:%02d:%02d", h, m, s)
        }

        intentService = Intent(context, CaloriesService::class.java)

        // register broadcast
        requireActivity().registerReceiver(updateUIReceiver, IntentFilter("UPDATE_UI"))

        val btn_play_pause = view.findViewById<ImageButton>(R.id.btn_play_pause_train)
        btn_play_pause.setOnClickListener {
            if (!isServiceStarted){ // the service is not started yet, so start it
                isServiceStarted = true

                // TODO: spostare in CaloriesService (?), in onCreate() ad esempio.
                val tPref = requireContext().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)

                //log di dati analitici
                val par = Bundle()
                val weight = tPref.getInt("Weight", 0)
                par.putString("play_btn", "start")
                par.putInt("weight", weight)
                firebaseAnalytics.logEvent("Button_start_pressed", par)

                // bind to CaloriesService
                intentService.putExtra("trainingType", trainingType)
                context?.bindService(intentService, connection, Context.BIND_AUTO_CREATE)

                // reset UI
                updateUI(0, 0, 0f)
            }

            if (!isPlayed) { // the play button is pressed
                btn_play_pause.setImageResource(R.drawable.pause_circle)

                // TODO: qui scrivi "training_resumed", dipende perché potrebbe essere la prima volta che preme su play.
                //log di dati analitici
                val par = Bundle()
                par.putString("play_btn", "training_resumed")

                firebaseAnalytics.logEvent("Button_resume_pressed", par)

                if(!isPaused) // the pause button was not previously pressed
                    pauseOffset = 0
                else{ // the pause button was previously pressed
                    isPaused = false
                    cService.setIsTrainingPaused(false)
                }
                chronometer.base = SystemClock.elapsedRealtime() + pauseOffset
                chronometer.start()
            }
            else { // the pause button is pressed
                btn_play_pause.setImageResource(R.drawable.play_circle)

                //log di dati analitici
                val par = Bundle()
                par.putString("play_btn", "training_paused")
                firebaseAnalytics.logEvent("Button_pause_pressed", par)

                cService.setIsTrainingPaused(true)
                isPaused = true
                chronometer.stop()
                pauseOffset = chronometer.base - SystemClock.elapsedRealtime()
            }
            isTraining = true
            isPlayed = !isPlayed
        }

        val btn_stop = view.findViewById<ImageButton>(R.id.btn_stop_train)
        btn_stop.setOnClickListener {
            if (isServiceStarted){  // stop the service if it is running
                isServiceStarted = false

                // reset UI
                updateUI(0, 0, 0f)
                chronometer.text = "00:00:00"

                // register the current training
                cService.registerTraining()

                unbindCS()

                // TODO: spostare in CaloriesService (?), in registerTraining() ad esempio
                //log di dati analitici
                val par = Bundle()
                par.putString("stop_btn", "training_stopped")
                firebaseAnalytics.logEvent("Button_stop_pressed", par)

                isPlayed = false
                isPaused = false
                pauseOffset = 0
                btn_play_pause.setImageResource(R.drawable.play_circle)
                //chronometer.base = SystemClock.elapsedRealtime()
                chronometer.stop()
            }

            /*if (isPlayed || isPaused) { // perform reset

                //log di dati analitici
                val par = Bundle()
                par.putString("stop_btn", "training_stopped")
                firebaseAnalytics.logEvent("Button_stop_pressed", par)

                isPlayed = false
                isPaused = false
                pauseOffset = 0
                btn_play_pause.setImageResource(R.drawable.play_circle)
                //chronometer.base = SystemClock.elapsedRealtime()
                chronometer.stop()
            }*/
            isTraining = false
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        updateUI(steps, distance, calories)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        unbindCS()
    }

    private val updateUIReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            steps = intent.getIntExtra("totalSteps", 0)
            distance = intent.getIntExtra("totalDistance", 0)
            calories = intent.getFloatExtra("totalCalories", 0f)

            updateUI(steps, distance, calories)
        }
    }

    private fun updateUI(totalSteps: Int, totalDistance: Int, totalCalories: Float){
        tv_totalSteps.text = totalSteps.toString()
        tv_totalDistance.text = totalDistance.toString()
        tv_totalCalories.text = totalCalories.roundToInt().toString()
        //tv_totalCalories.text = String.format("%.${3}f", totalCalories)
    }

    // Unbind this CaloriesService if not bounded
    private fun unbindCS(){
        if (isServiceBounded) {
            context?.unbindService(connection)
            isServiceBounded = false
        }
    }
}