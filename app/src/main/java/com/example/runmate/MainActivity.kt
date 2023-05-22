package com.example.runmate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    //private lateinit var username: String?
    private val bundle = Bundle()

    private var selectedItemId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_nav)

        val sharedPref = getSharedPreferences("TRAINING_DATA", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            if (sharedPref.getString("currentDate", "") != currentDate){ // the stats are reset each day (apart from training list)
                val editor = sharedPref.edit()
                editor.remove("totalSteps")
                editor.remove("totalDistance")
                editor.remove("totalCalories")
                editor.apply()
            }
        }

        // REMOVE THIS, IT'S JUST FOR DEBUG
        /*sharedPref?.edit()?.apply {
            remove("trainingList")
            remove("totalSteps")
            remove("totalDistance")
            remove("totalCalories")
            apply()
        }*/

        val sPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val statsFragment = StatsFragment()
        val trainingChoiceFragment = TrainingChoiceFragment()
        val trainingFragment = TrainingFragment()
        val userFragment = UserFragment()

        val username = sPref.getString("username", "")
        bundle.putString("USERNAME", username)
        userFragment.arguments = bundle

        setCurrentFragment(statsFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            selectedItemId = item.itemId
            when (item.itemId) {
                R.id.stats -> {
                    if (TrainingFragment.isTraining) {
                        bottomNavigationView.post {
                            bottomNavigationView.selectedItemId = R.id.training
                        }
                    }
                    else setCurrentFragment(statsFragment)
                }
                R.id.training -> {
                    if (TrainingFragment.isTraining) {
                        showTrainingAlert()
                    }
                    else setCurrentFragment(trainingChoiceFragment)
                }
                R.id.user -> {
                    if (TrainingFragment.isTraining) {
                        bottomNavigationView.post {
                            bottomNavigationView.selectedItemId = R.id.training
                        }
                    }
                    else setCurrentFragment(userFragment)
                    // if (::username.isInitialized) {
                    //     userFragment.arguments = bundle
                    //     setCurrentFragment(userFragment)
                    // } else {
                    //     val loadingDialog = ProgressDialog.show(this, "", "Un attimo...", true)
                    //     loadUsername({loadingDialog.dismiss()
                    //                     userFragment.arguments = bundle
                    //                     setCurrentFragment(userFragment)})
                    // }
                }
            }
            true
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.findFragmentById(R.id.flFragment) is TrainingFragment) {
                    if (TrainingFragment.isTraining)
                        showTrainingAlert()
                    else {
                        val fragment = TrainingChoiceFragment()
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.flFragment, fragment)
                        transaction.commit()
                    }
                }
                else finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

    // Shows an alert to tell the user that an activity is being tracked
    private fun showTrainingAlert() {
        val title = "Attenzione!"
        val message = "Stai registrando un'attività:\npremi il pulsante di stop per fermarla e permetterne il salvataggio."
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

//    @AddTrace(name = "loadUsername", enabled = true)
//    //il parametro è una funzione di callback
//    private fun loadUsername(onLoaded : () -> Unit) {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid
//        val userRef =
//            Firebase.database("https://runmate-b7137-default-rtdb.europe-west1.firebasedatabase.app/")
//                .getReference("users/$uid")
//
//        userRef.child("username").get().addOnSuccessListener { dataSnapshot ->
//            username = dataSnapshot.getValue(String::class.java).toString()
//            bundle.putString("USERNAME", username)
//            onLoaded() // apri lo userFragment una volta terminata la chiamata asincrona al DB
//
//        }
//    }
}