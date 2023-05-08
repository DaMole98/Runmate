package com.example.runmate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class Profile : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val btn = findViewById<ImageButton>(R.id.profilebutton)
        btn.setImageResource(R.drawable.profilo_black)

        val training = findViewById<ImageButton>(R.id.trainingbutton)
        training.setOnClickListener {
            val intent = Intent(this, Training::class.java)
            startActivity(intent)
        }

        val stats = findViewById<ImageButton>(R.id.statsbutton)
        stats.setOnClickListener {
            val intent = Intent(this, Stats::class.java)
            startActivity(intent)
        }

        /*val settings = findViewById<ImageButton>(R.id.settingsbutton)
        settings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }*/
    }
}