package com.example.runmate


import android.content.Context
import com.google.firebase.perf.FirebasePerformance
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runmate.utils.CloudDBSingleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.material.dialog.MaterialAlertDialogBuilder



class Registration : AppCompatActivity() {

    private lateinit var editUsername: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirmPassword: EditText
    private lateinit var register_btn: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var back_btn : Button
    private lateinit var textView: TextView
    private var DB = CloudDBSingleton.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = Firebase.auth
        editUsername = findViewById<EditText>(R.id.username)
        editEmail = findViewById<EditText>(R.id.email)
        editPassword = findViewById<EditText>(R.id.password)
        editConfirmPassword = findViewById<EditText>(R.id.confirm_password)
        register_btn = findViewById(R.id.btn_register)
        back_btn = findViewById(R.id.btn_back)

        val fbPer = FirebasePerformance.getInstance()
        val regTrace = fbPer.newTrace(" user_registration_trace")

        back_btn.setOnClickListener {
            intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
        }

        register_btn.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()
            val username = editUsername.text.toString()

            if(username.isNullOrBlank()){
                Toast.makeText(this, "Inserisci un nome utente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (username.length > 30){
                Toast.makeText(this, "Il nome utente può contenere al massimo 30 caratteri", Toast.LENGTH_SHORT).show()
            }
            if (email.isNullOrBlank()) {
                Toast.makeText(this, "Inserisci la tua email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isNullOrBlank()) {
                Toast.makeText(this, "Inserisci la password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isNullOrBlank()) {
                Toast.makeText(this, "Conferma la password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "La password non corrisponde", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(password.length < 6) {
                Toast.makeText(this, "La password deve contenere almeno 6 caratteri", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else {


                regTrace.start()
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this)
                { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        val uid = user!!.uid
                        saveUserData(uid, username, email)

                        Toast.makeText(this, "Benvenuto in Runmate!", Toast.LENGTH_SHORT).show()

                     //   val alertDialog = MaterialAlertDialogBuilder(this)
                     //       .setMessage("Prima di cominciare, ci serve qualche altra informazione...")
                     //       .setPositiveButton("OK") { dialog, which ->
//
                     //           val intent = Intent(this, TargetActivity::class.java)
                     //           startActivity(intent)
                     //           finish()
                     //       }
                     //       .setCancelable(false)
                     //       .create()
//
                     //   alertDialog.show()


                        //TODO: cambiare intent per lanciare l'activity di profiling invece del loign
                        intent = Intent(applicationContext, TargetActivity::class.java)
                        startActivity(intent)
                    } else Toast.makeText(
                        baseContext,
                        "Registrazione fallita.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                regTrace.stop()
            }

        }
    }

    private fun saveUserData( uid: String, username : String, email : String){
        //val database = Firebase.database("https://runmate-b7137-default-rtdb.europe-west1.firebasedatabase.app/").reference
        //val database = (application as Runmate).database //ottieni l'istanza (singleton) del database dalla classe applicaiton
        val database = DB.getDBref()

        val databaseRef = database.reference

        val sPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sPref.edit()
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("uid", uid)
        editor.apply()

        //push user data into cloud database
        val userData: MutableMap<String, Any> = HashMap()
        userData["uid"] = uid
        userData["username"] = username
        userData["email"] = email
        val usersRef = databaseRef.child("users")
        val userRef = usersRef.child(uid)

        userRef.setValue(userData)
        userRef.child("trainingList").setValue(null)
        userRef.child("profile").setValue(null)

    }
}