package com.example.runmate.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth

fun checkCurrentUser(mAuth: FirebaseAuth): Boolean {
    val currentUser = mAuth.currentUser
    return if (currentUser != null) {
        currentUser.reload()
        true
    }
    else false
}

fun setUserProperties(context: Context, analytics: FirebaseAnalytics){

    val uid = FirebaseAuth.getInstance().currentUser!!.uid
    val tPref = context.getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)
    val weight = tPref.getInt("Weight", 0)
    val height = tPref.getInt("Height", 0)
    val gender = tPref.getString("Gender", "Male")


    analytics.setUserProperty("Weight", weight.toString())
    analytics.setUserProperty("Height", height.toString())
    analytics.setUserProperty("Gender", gender.toString())

}

fun trackDevice(resources: Resources, analytics : FirebaseAnalytics) {
    val deviceModel = Build.MODEL
    val osVersion = Build.VERSION.RELEASE
    val language = resources.configuration.locales[0].language

    val bundle = Bundle()
    bundle.putString("Device_Model", deviceModel)
    bundle.putString("OS_Version", osVersion)
    bundle.putString("Language", language)

    analytics.logEvent("Device_Properties", bundle)
}