package com.example.runmate

import com.example.runmate.utils.*
import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class Runmate : Application() {

    private lateinit var database : FirebaseDatabase

    override fun onCreate() {
        super.onCreate()
        //val databaseUrl = "https://runmate-b7137-default-rtdb.europe-west1.firebasedatabase.app/"
        //database = FirebaseDatabase.getInstance(databaseUrl)
        database = CloudDBSingleton.getInstance().getDBref()
       // database.setPersistenceEnabled(true)

    }
}
