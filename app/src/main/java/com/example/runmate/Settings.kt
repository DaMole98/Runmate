package com.example.runmate

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class Settings : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btn = findViewById<ImageButton>(R.id.settingsbutton)
        btn.setImageResource(R.drawable.settings_black)

        val training = findViewById<ImageButton>(R.id.trainingbutton)
        training.setOnClickListener {
            val intent = Intent(this, Training::class.java)
            startActivity(intent)
        }

        val profile = findViewById<ImageButton>(R.id.profilebutton)
        profile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        val stats = findViewById<ImageButton>(R.id.statsbutton)
        stats.setOnClickListener {
            val intent = Intent(this, Stats::class.java)
            startActivity(intent)
        }
    }
}