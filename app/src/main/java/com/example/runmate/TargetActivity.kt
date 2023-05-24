package com.example.runmate

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics


class TargetActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_target)

        val confirm_btn = findViewById<Button>(R.id.confirm_target_button)
        val reset_btn = findViewById<Button>(R.id.reset_target_button)
        val height_edit = findViewById<EditText>(R.id.editTargetAge)
        val weight_edit = findViewById<EditText>(R.id.editTargetWeight)
        val steps_edit = findViewById<EditText>(R.id.editTargetSteps)
        val kcal_edit = findViewById<EditText>(R.id.editTargetKcal)
        val meters_edit = findViewById<EditText>(R.id.editTargetMeter)
        val gender = findViewById<RadioGroup>(R.id.editGender)

       // val firebaseAnalytics = FirebaseAnalytics.getInstance(this)


        reset_btn.setOnClickListener {
            height_edit.setText("0")
            weight_edit.setText("0")
            steps_edit.setText("0")
            kcal_edit.setText("0")
            meters_edit.setText("0")
            gender.clearCheck()
        }

        confirm_btn.setOnClickListener {
            val sharedPreferences = getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("Height", Integer.parseInt(height_edit.text.toString()))
            editor.putInt("Weight", Integer.parseInt(weight_edit.text.toString()))
            editor.putInt("Steps", Integer.parseInt(steps_edit.text.toString()))
            editor.putInt("Calories", Integer.parseInt(kcal_edit.text.toString()))
            editor.putInt("Meters", Integer.parseInt(meters_edit.text.toString()))
            editor.apply()

            //firebaseAnalytics.setUserProperty("weight", weight_edit.text.toString())

            Toast.makeText(this, "Campi modificati correttamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked
            val sharedPreferences = getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.man ->
                    if (checked) {
                        editor.putString("Gender", "Man")
                        editor.apply()
                    }
                R.id.woman ->
                    if (checked) {
                        editor.putString("Gender", "Woman")
                        editor.apply()
                    }
            }
        }
    }
}