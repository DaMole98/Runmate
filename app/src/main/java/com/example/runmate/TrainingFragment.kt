package com.example.runmate

import android.app.Service
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
import kotlin.math.roundToInt

import com.google.firebase.analytics.FirebaseAnalytics


class TrainingFragment:Fragment(R.layout.fragment_training) {
    private lateinit var mService: CaloriesService
    private lateinit var intentService: Intent
    private var mBound: Boolean = false

    private lateinit var tv_totalSteps: TextView
    private lateinit var tv_totalDistance: TextView
    private lateinit var tv_totalCalories: TextView
    private lateinit var chronometer: Chronometer

    private var isStarted = false
    private var isPaused = false
    private var isServiceStarted = false
    private var pauseOffset: Long = 0

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
            mService = binder.getService()
            mService.trainingType = trainingType
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
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
        isStarted = false
        isPaused = false
        isServiceStarted = false

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
            chronometer.text = String.format("%01d:%02d:%02d", h, m, s)
        }

        // bind to CaloriesService
        intentService = Intent(context, CaloriesService::class.java)
        context?.bindService(intentService, connection, Context.BIND_AUTO_CREATE)

        val btn_play_pause = view.findViewById<ImageButton>(R.id.btn_play_pause_train)
        btn_play_pause.setOnClickListener {
            if (!isServiceStarted){

                //log di dati analitici
                val par = Bundle()
                par.putString("play_btn", "start")
                firebaseAnalytics.logEvent("Button_start_pressed", par)

                isServiceStarted = true
                updateUI(0, 0, 0f)
                mService.startService(intentService)
            }

            if (!isStarted) {

                btn_play_pause.setImageResource(R.drawable.pause_circle)

                //log di dati analitici
                val par = Bundle()
                par.putString("play_btn", "training_resumed")
                firebaseAnalytics.logEvent("Button_resume_pressed", par)

                if(!isPaused)
                    chronometer.base = SystemClock.elapsedRealtime()
                else{
                    isPaused = false
                    mService.isTrainingPaused = !mService.isTrainingPaused
                    chronometer.base = SystemClock.elapsedRealtime() + pauseOffset
                }
                chronometer.start()
            }
            else {
                btn_play_pause.setImageResource(R.drawable.play_circle)

                //log di dati analitici
                val par = Bundle()
                par.putString("play_btn", "training_paused")
                firebaseAnalytics.logEvent("Button_pause_pressed", par)

                mService.isTrainingPaused = !mService.isTrainingPaused
                isPaused = true
                chronometer.stop()
                pauseOffset = chronometer.base - SystemClock.elapsedRealtime()
            }
            isTraining = true
            isStarted = !isStarted
        }


        val btn_stop = view.findViewById<ImageButton>(R.id.btn_stop_train)
        btn_stop.setOnClickListener {
            if (isServiceStarted){
                isServiceStarted = false
                //requireActivity().sendBroadcast(Intent("STOP_SERVICE"))
                mService.registerTraining()
                mService.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                mService.stopService(intentService)
            }

            if (isStarted || isPaused) {

                //log di dati analitici
                val par = Bundle()
                par.putString("stop_btn", "training_stopped")
                firebaseAnalytics.logEvent("Button_stop_pressed", par)

                isStarted = false
                isPaused = false
                pauseOffset = 0
                btn_play_pause.setImageResource(R.drawable.play_circle)
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.stop()
            }
            isTraining = false
        }

        requireActivity().registerReceiver(updateUIReceiver, IntentFilter("UPDATE_UI"))

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mService.stopService(intentService)
        context?.unbindService(connection)
        mBound = false
    }



    fun updateUI(totalSteps: Int, totalDistance: Int, totalCalories: Float){
        tv_totalSteps.text = totalSteps.toString()
        tv_totalDistance.text = totalDistance.toString()
        tv_totalCalories.text = totalCalories.roundToInt().toString()
    }




    private val updateUIReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val totalSteps = intent.getIntExtra("totalSteps", 0)
            val totalDistance = intent.getIntExtra("totalDistance", 0)
            // TODO(tv_totalCalories.text = "${intent?.getFloatExtra("totalCalories", 0f)?.roundToInt()}")
            val totalCalories = intent.getFloatExtra("totalCalories", 0f)
            updateUI(totalSteps, totalDistance, totalCalories)
        }
    }

}