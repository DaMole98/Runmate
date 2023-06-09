package com.example.runmate

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val bundle = Bundle()

    private var selectedItemId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_nav)

        // check permissions to use the step sensor
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // get current user id and remove daily stats if this is a new day
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val sharedPref = getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)
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

        val statsFragment = StatsFragment()
        val trainingChoiceFragment = TrainingChoiceFragment()
        val userFragment = UserFragment()

        val username = sharedPref.getString("username", "")
        bundle.putString("USERNAME", username)
        userFragment.arguments = bundle

        setCurrentFragment(statsFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            selectedItemId = item.itemId

            // get instance of TrainingFragment (if any) and handle user navigation between fragments
            val tfInstance = supportFragmentManager.findFragmentByTag("TrainingFragment") as? TrainingFragment
            when (item.itemId) {
                R.id.stats -> {
                    if (tfInstance?.getIsTraining() == true) { // the user is training => don't change current fragment
                        bottomNavigationView.post {
                            bottomNavigationView.selectedItemId = R.id.training
                        }
                    }
                    else setCurrentFragment(statsFragment)
                }
                R.id.training -> {
                    if (tfInstance?.getIsTraining() == true) { // the user is training => don't change current fragment
                        showTrainingAlert("Stai registrando un'attività:\npremi il pulsante di stop per fermarla e permetterne il salvataggio.")
                    }
                    else setCurrentFragment(trainingChoiceFragment)
                }
                R.id.user -> {
                    if (tfInstance?.getIsTraining() == true) { // the user is training => don't change current fragment
                        bottomNavigationView.post {
                            bottomNavigationView.selectedItemId = R.id.training
                        }
                    }
                    else setCurrentFragment(userFragment)
                }
            }
            true
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.findFragmentById(R.id.flFragment) is TrainingFragment) { // the user is training => don't change current fragment
                    val tfInstance = supportFragmentManager.findFragmentByTag("TrainingFragment") as TrainingFragment
                    if (tfInstance.getIsTraining())
                        showTrainingAlert("Stai registrando un'attività:\npremi il pulsante di stop per fermarla e permetterne il salvataggio.")
                    else setCurrentFragment(trainingChoiceFragment)
                    /*else {
                        val fragment = TrainingChoiceFragment()
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.flFragment, fragment)
                        transaction.commit()
                    }*/
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
    private fun showTrainingAlert(message: String) {
        title = "Attenzione!"
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // Register the permissions callback
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (!isGranted) // permission is not granted
            showTrainingAlert("Hai rifiutato l'accesso al sensore contapassi: non potrai registrare i tuoi allenamenti.")
    }
}