package com.example.runmate

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Explode
import android.view.Window
import android.widget.ImageButton

class Stats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val btn = findViewById<ImageButton>(R.id.statsbutton)
        btn.setImageResource(R.drawable.stats_black)

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

        this.overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);
        /*val settings = findViewById<ImageButton>(R.id.settingsbutton)
        settings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }*/
    }
}