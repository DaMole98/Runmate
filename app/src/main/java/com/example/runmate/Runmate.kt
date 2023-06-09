package com.example.runmate
import com.example.runmate.utils.*
import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics


class Runmate : Application() {

    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()

        analytics = FirebaseAnalytics.getInstance(this)
        trackDevice(resources, analytics)
    }
}