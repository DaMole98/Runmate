package com.example.runmate

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class Runmate : Application() {

    public lateinit var database : FirebaseDatabase

    override fun onCreate() {
        super.onCreate()
        val databaseUrl = "https://runmate-b7137-default-rtdb.europe-west1.firebasedatabase.app/"
        database = FirebaseDatabase.getInstance(databaseUrl)
        database.setPersistenceEnabled(true)
    }
}
