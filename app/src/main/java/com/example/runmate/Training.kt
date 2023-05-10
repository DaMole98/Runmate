package com.example.runmate

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class Training : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_training)

        val btn = findViewById<ImageButton>(R.id.trainingbutton)
        btn.setImageResource(R.drawable.allenamento_black)

        val stats = findViewById<ImageButton>(R.id.statsbutton)
        stats.setOnClickListener {
            val intent = Intent(this, Stats::class.java)
            startActivity(intent)
        }

        val profile = findViewById<ImageButton>(R.id.profilebutton)
        profile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }


        /*val settings = findViewById<ImageButton>(R.id.settingsbutton)
        settings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }*/
    }
}