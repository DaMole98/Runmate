package com.example.runmate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TrainingFragment:Fragment(R.layout.fragment_training) {
    private lateinit var tv_totalSteps: TextView
    private lateinit var tv_totalDistance: TextView
    private lateinit var tv_totalCalories: TextView
    private lateinit var chronometer: Chronometer

    private var isStarted = false
    private var isPaused = false
    private var isServiceStarted = false
    var pauseOffset: Long = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_training, container, false)

        tv_totalSteps = view.findViewById(R.id.tv_steps_train)
        tv_totalDistance = view.findViewById(R.id.tv_calories_train)
        tv_totalCalories = view.findViewById(R.id.tv_distance_train)
        chronometer = view.findViewById(R.id.chronometer_train)

        chronometer.setOnChronometerTickListener {
            val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
            val elapsedSeconds = elapsedMillis / 1000
            val h = elapsedSeconds / 3600
            val m = (elapsedSeconds % 3600) / 60
            val s = elapsedSeconds % 60
            val elapsedFormatted = String.format("%01d:%02d:%02d", h, m, s)
            chronometer.text = elapsedFormatted
        }

        val btn_play_pause = view.findViewById<ImageButton>(R.id.btn_play_pause_train)
        btn_play_pause.setOnClickListener {
            if (!isServiceStarted){
                isServiceStarted = true
                // Start the service
                val serviceIntent = Intent(context, CaloriesService::class.java)
                context?.startService(serviceIntent)
            }

            if (!isStarted) {
                btn_play_pause.setImageResource(R.drawable.pause_circle)

                if(!isPaused)
                    chronometer.base = SystemClock.elapsedRealtime()
                else{
                    isPaused = false
                    chronometer.base = SystemClock.elapsedRealtime() + pauseOffset
                }
                chronometer.start()
            }
            else {
                btn_play_pause.setImageResource(R.drawable.play_circle)
                isPaused = true
                chronometer.stop()
                pauseOffset = chronometer.base - SystemClock.elapsedRealtime()
            }
            isStarted = !isStarted
        }

        val btn_stop = view.findViewById<ImageButton>(R.id.btn_stop_train)
        btn_stop.setOnClickListener {
            if (isServiceStarted){
                isServiceStarted = false
                requireActivity().sendBroadcast(Intent("STOP_SERVICE"))
            }

            if (isStarted || isPaused) {
                isStarted = false
                isPaused = false
                pauseOffset = 0
                btn_play_pause.setImageResource(R.drawable.play_circle)
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.stop()
            }
        }

        requireActivity().registerReceiver(updateUIReceiver, IntentFilter("UPDATE_UI"))

        return view
    }

    override fun onDestroyView() {
        requireActivity().unregisterReceiver(updateUIReceiver)

        super.onDestroyView()
    }

    private val updateUIReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            tv_totalSteps.text = intent?.getIntExtra("totalSteps", 0).toString()
            tv_totalDistance.text = "${intent?.getIntExtra("totalDistance", 0)} m"

            // TODO(tv_totalCalories.text = "${intent?.getFloatExtra("totalCalories", 0f)?.roundToInt()} kcal")
            tv_totalCalories.text = String.format("%.${3}f kcal", intent?.getFloatExtra("totalCalories", 0f))
        }
    }
}