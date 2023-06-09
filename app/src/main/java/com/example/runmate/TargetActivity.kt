package com.example.runmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runmate.utils.setUserProperties
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth


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


        // TODO: sistemare le sharedPreferences in modo da evitare che vengano prese quelle di account precendenti

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val sharedPreferences = getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)
        //val sharedPreferences = getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE)
        val allEntries: Map<String,*> = sharedPreferences.all

        //Nel caso in cui esistano dei valori salvati allora vengono mostrati al posto dei valori di default


            height_edit.setText(sharedPreferences.getInt("Height",0).toString())
            weight_edit.setText(sharedPreferences.getInt("Weight",0).toString())
            steps_edit.setText(sharedPreferences.getInt("Steps",0).toString())
            kcal_edit.setText(sharedPreferences.getInt("Calories",0).toString())
            meters_edit.setText(sharedPreferences.getInt("Meters",0).toString())
            gender.check(R.id.male)
            val g = sharedPreferences.getString("Gender", "")
            if(g == "" || g == "Male") gender.check(R.id.male)
            else gender.check(R.id.female)
        //}

        //Premere reset porta tutti i valori numerici a 0 e annulla la selezione del gender

        reset_btn.setOnClickListener {
            height_edit.setText("0")
            weight_edit.setText("0")
            steps_edit.setText("0")
            kcal_edit.setText("0")
            meters_edit.setText("0")
            gender.clearCheck()
        }

        //Controllo sul valore "Altezza"

        height_edit.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("Not yet implemented")
            }
            override fun afterTextChanged(s: Editable?)
            {
                val value = s.toString().toIntOrNull()
                if(value !=null && s != null && value > 349)
                {
                    Toast.makeText(applicationContext, "Altezza non valida, inserisci un valore tra 1 e 350", Toast.LENGTH_SHORT).show()
                    height_edit.text =(s.subSequence(0,s.length-1) as Editable)
                    height_edit.setSelection(s.length-1)
                }
            }
        })

        //Controllo sul valore "Peso"

        weight_edit.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("Not yet implemented")
            }
            override fun afterTextChanged(s: Editable?)
            {
                val value = s.toString().toIntOrNull()
                if(value !=null && s != null && value > 349)
                {
                    Toast.makeText(applicationContext, "Peso non valido, inserisci un valore tra 1 e 350", Toast.LENGTH_SHORT).show()
                    weight_edit.text =(s.subSequence(0,s.length-1) as Editable)
                    weight_edit.setSelection(s.length-1)
                }
            }
        })

        //Controllo sul valore "Metri giornalieri"

        steps_edit.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("Not yet implemented")
            }
            override fun afterTextChanged(s: Editable?)
            {
                val value = s.toString().toIntOrNull()
                if(value !=null && s != null && value > 49999)
                {
                    Toast.makeText(applicationContext, "Numero di passi non valido, inserisci un valore tra 1 e 50000", Toast.LENGTH_SHORT).show()
                    steps_edit.text =(s.subSequence(0,s.length-1) as Editable)
                    steps_edit.setSelection(s.length-1)
                }
            }
        })

        //Controllo sul valore "Calorie giornaliere"

        kcal_edit.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("Not yet implemented")
            }
            override fun afterTextChanged(s: Editable?)
            {
                val value = s.toString().toIntOrNull()
                if(value !=null && s != null && value > 4999)
                {
                    Toast.makeText(applicationContext, "Numero di calorie non valido, inserisci un valore tra 1 e 5000", Toast.LENGTH_SHORT).show()
                    kcal_edit.text =(s.subSequence(0,s.length-1) as Editable)
                    kcal_edit.setSelection(s.length-1)
                }
            }
        })

        //Controllo sul valore "Metri giornalieri"

        meters_edit.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("Not yet implemented")
            }
            override fun afterTextChanged(s: Editable?)
            {
                val value = s.toString().toIntOrNull()
                if(value !=null && s != null && value > 29999)
                {
                    Toast.makeText(applicationContext, "Numero di metri non valido, inserisci un valore tra 1 e 30000", Toast.LENGTH_SHORT).show()
                    meters_edit.text =(s.subSequence(0,s.length-1) as Editable)
                    meters_edit.setSelection(s.length-1)
                }
            }
        })


        //Premere il tasto conferma salva tutti i valori nelle sharedPreferences e riporta alla schermata principale

        confirm_btn.setOnClickListener {

            //Controllo sui campi vuoti

            if(height_edit.text.isEmpty() || weight_edit.text.isEmpty() || steps_edit.text.isEmpty() || kcal_edit.text.isEmpty() || meters_edit.text.isEmpty() || (gender.checkedRadioButtonId != R.id.male && gender.checkedRadioButtonId != R.id.female))
                check=6
            else{
                //Controllo sulla validità dei dati
                if(Integer.parseInt(meters_edit.text.toString()) <= 0 || Integer.parseInt(meters_edit.text.toString()) > 50000)
                    check=1
                if(Integer.parseInt(kcal_edit.text.toString()) <= 0 || Integer.parseInt(kcal_edit.text.toString()) > 5000)
                    check=2
                if(Integer.parseInt(steps_edit.text.toString()) <= 0 || Integer.parseInt(steps_edit.text.toString()) > 50000)
                    check=3
                if(Integer.parseInt(weight_edit.text.toString()) <= 0 || Integer.parseInt(weight_edit.text.toString()) > 350)
                    check=4
                if(Integer.parseInt(height_edit.text.toString()) <= 0 || Integer.parseInt(height_edit.text.toString()) > 350)
                    check=5
            }
            //Utilizzo il when per far apparire un allert alla volta

            when(check){
                0 -> {
                    val height = Integer.parseInt(height_edit.text.toString())
                    val weight = Integer.parseInt(weight_edit.text.toString())
                    val steps = Integer.parseInt(steps_edit.text.toString())
                    val calories = Integer.parseInt(kcal_edit.text.toString())
                    val meters = Integer.parseInt(meters_edit.text.toString())

                    val editor = sharedPreferences.edit()
                    editor.putInt("Height", height)
                    editor.putInt("Weight", weight)
                    editor.putInt("Steps", steps)
                    editor.putInt("Calories", calories)
                    editor.putInt("Meters", meters)


                    when (gender.checkedRadioButtonId) {
                        R.id.male -> editor.putString("Gender", "Male")
                        R.id.female -> editor.putString("Gender", "Female")
                    }
                    editor.apply()
                    Toast.makeText(this, "Campi modificati correttamente", Toast.LENGTH_SHORT).show()
                    val analytics = FirebaseAnalytics.getInstance(applicationContext)
                    setUserProperties(applicationContext, analytics)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                1 -> Toast.makeText(this, "Numero di metri non valido, inserisci un valore tra 1 e 50000", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(this, "Numero di calorie non valido, inserisci un valore tra 1 e 5000", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(this, "Numero di passi non valido, inserisci un valore tra 1 e 50000", Toast.LENGTH_SHORT).show()
                4 -> Toast.makeText(this, "Peso non valido, inserisci un valore tra 1 e 350", Toast.LENGTH_SHORT).show()
                5 -> Toast.makeText(this, "Altezza non valida, inserisci un valore tra 1 e 350", Toast.LENGTH_SHORT).show()
                6 -> Toast.makeText(this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show()
            }

            check = 0
        }
    }

}