package com.example.runmate

import android.content.Context
import android.content.Intent
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
        val height_edit = findViewById<EditText>(R.id.editTargetHeight)
        val weight_edit = findViewById<EditText>(R.id.editTargetWeight)
        val steps_edit = findViewById<EditText>(R.id.editTargetSteps)
        val kcal_edit = findViewById<EditText>(R.id.editTargetKcal)
        val meters_edit = findViewById<EditText>(R.id.editTargetMeter)
        val gender = findViewById<RadioGroup>(R.id.editGender)
        var check = 0

       // val firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // TODO: sistemare le sharedPreferences in modo da evitare che vengano prese quelle di account precendenti

        val sharedPreferences = getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE)
        val allEntries: Map<String,*> = sharedPreferences.all

        //Nel caso in cui esistano dei valori salvati allora vengono mostrati al posto dei valori di default

        if(allEntries.isNotEmpty())
        {
            height_edit.setText(sharedPreferences.getInt("Height",0).toString())
            weight_edit.setText(sharedPreferences.getInt("Weight",0).toString())
            steps_edit.setText(sharedPreferences.getInt("Steps",0).toString())
            kcal_edit.setText(sharedPreferences.getInt("Calories",0).toString())
            meters_edit.setText(sharedPreferences.getInt("Meters",0).toString())
            if(sharedPreferences.getString("Gender", "").equals("Male"))
                gender.check(R.id.male)
            else
                gender.check(R.id.female)
        }

        //Premere reset porta tutti i valori numerici a 0 e annulla la selezione del gender

        reset_btn.setOnClickListener {
            height_edit.setText("0")
            weight_edit.setText("0")
            steps_edit.setText("0")
            kcal_edit.setText("0")
            meters_edit.setText("0")
            gender.clearCheck()
        }

        //Premere il tasto conferma salva tutti i valori nelle sharedPreferences e riporta alla schermata principale

        confirm_btn.setOnClickListener {

            //Controllo sui campi vuoti

            if(height_edit.text.isEmpty() || weight_edit.text.isEmpty() || steps_edit.text.isEmpty() || kcal_edit.text.isEmpty() || meters_edit.text.isEmpty() || (gender.checkedRadioButtonId != R.id.male && gender.checkedRadioButtonId != R.id.female))
                check++

            //Controllo sulla validità dei dati

            if(Integer.parseInt(meters_edit.text.toString()) <= 0 || Integer.parseInt(meters_edit.text.toString()) >= 50000)
                check++
            if(Integer.parseInt(kcal_edit.text.toString()) <= 0 || Integer.parseInt(kcal_edit.text.toString()) >= 500)
                check++
            if(Integer.parseInt(steps_edit.text.toString()) <= 0 || Integer.parseInt(steps_edit.text.toString()) >= 50000)
                check++
            if(Integer.parseInt(weight_edit.text.toString()) <= 0 || Integer.parseInt(weight_edit.text.toString()) >= 350)
                check++
            if(Integer.parseInt(height_edit.text.toString()) <= 0 || Integer.parseInt(height_edit.text.toString()) >= 350)
                check++

            //Utilizzo il when per far apparire un allert alla volta

            when(check){
                0 -> {
                    val editor = sharedPreferences.edit()
                    editor.putInt("Height", Integer.parseInt(height_edit.text.toString()))
                    editor.putInt("Weight", Integer.parseInt(weight_edit.text.toString()))
                    editor.putInt("Steps", Integer.parseInt(steps_edit.text.toString()))
                    editor.putInt("Calories", Integer.parseInt(kcal_edit.text.toString()))
                    editor.putInt("Meters", Integer.parseInt(meters_edit.text.toString()))
                    when (gender.checkedRadioButtonId) {
                        R.id.male -> editor.putString("Gender", "Male")
                        R.id.female -> editor.putString("Gender", "Female")
                    }
                    editor.apply()
                    Toast.makeText(this, "Campi modificati correttamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                1 -> Toast.makeText(this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(this, "Numero di metri non valido, inserisci un valore tra 1 e 49999", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(this, "Numero di calorie non valido, inserisci un valore tra 1 e 500", Toast.LENGTH_SHORT).show()
                4 -> Toast.makeText(this, "Numero di passi non valido, inserisci un valore tra 1 e 49999", Toast.LENGTH_SHORT).show()
                5 -> Toast.makeText(this, "Peso non valido, inserisci un valore tra 1 e 350", Toast.LENGTH_SHORT).show()
                6 -> Toast.makeText(this, "Altezza non valida, inserisci un valore tra 1 e 350", Toast.LENGTH_SHORT).show()
            }

            check = 0
            //firebaseAnalytics.setUserProperty("weight", weight_edit.text.toString())
        }
    }
}