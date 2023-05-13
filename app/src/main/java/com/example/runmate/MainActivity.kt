package com.example.runmate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.metrics.AddTrace

class MainActivity : AppCompatActivity() {

    private lateinit var username: String
    private val bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadUsername()

        setContentView(R.layout.activity_bottom_nav)

        val statsFragment = StatsFragment()
        val trainingFragment = TrainingFragment()
        val userFragment = UserFragment()

        setCurrentFragment(statsFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.stats -> setCurrentFragment(statsFragment)
                R.id.training -> setCurrentFragment(trainingFragment)
                R.id.user -> {
                    if(::username.isInitialized) {
                        userFragment.arguments = bundle
                        setCurrentFragment(userFragment)
                    }
                }

            }
            true
        }


    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

    @AddTrace(name = "loadUsername", enabled = true)
    private fun loadUsername() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef =
            Firebase.database("https://runmate-b7137-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users/$uid")

        userRef.child("username").get().addOnSuccessListener { dataSnapshot ->
            username = dataSnapshot.getValue(String::class.java).toString()
            bundle.putString("USERNAME", username)
        }
    }
}