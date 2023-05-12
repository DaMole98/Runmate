package com.example.runmate

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import kotlin.math.roundToInt

class Training : Activity() {
    private lateinit var tv_totalSteps: TextView
    private lateinit var tv_totalDistance: TextView
    private lateinit var tv_totalCalories: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_training)

       // val btn = findViewById<ImageButton>(R.id.trainingbutton)
       // btn.setImageResource(R.drawable.allenamento_black)
//
       // val stats = findViewById<ImageButton>(R.id.statsbutton)
       // stats.setOnClickListener {
       //     val intent = Intent(this, Stats::class.java)
       //     startActivity(intent)
       // }
//
       // val profile = findViewById<ImageButton>(R.id.profilebutton)
       // profile.setOnClickListener {
       //     val intent = Intent(this, Profile::class.java)
       //     startActivity(intent)
       // }

        /*val settings = findViewById<ImageButton>(R.id.settingsbutton)
        settings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }*/

        ////// DA QUI CODICE PER CONTAPASSI (ASSIEME A CaloriesService)

        registerReceiver(updateUIReceiver, IntentFilter("UPDATE_UI"))

        // Start the service
        val serviceIntent = Intent(this, CaloriesService::class.java)
        startService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(updateUIReceiver)
        sendBroadcast(Intent("STOP_SERVICE"))
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