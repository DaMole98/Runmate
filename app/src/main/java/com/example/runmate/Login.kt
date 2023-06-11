package com.example.runmate


import com.example.runmate.utils.*
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.metrics.AddTrace

class Login : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginscreen)

        mAuth = Firebase.auth
        editEmail = findViewById<EditText>(R.id.email)
        editPassword = findViewById<EditText>(R.id.password)
        loginBtn = findViewById(R.id.btn_login)

        //check if the user is already logged in
        if(checkCurrentUser(mAuth)) {
            Toast.makeText(baseContext, "Benvenuto", Toast.LENGTH_LONG).show()
            val analytics = FirebaseAnalytics.getInstance(applicationContext)
            setUserProperties(applicationContext, analytics) //set analytics user's parameters
            loadMainActivity()
        }

        else Loadlogin()

    }

    /*
    Load login view
    */
    private fun Loadlogin(){

        registerBtn = findViewById(R.id.btn_register)
        registerBtn.setOnClickListener{ view ->
            val intent = Intent(view.context, Registration::class.java)
            startActivity(intent)
            finish()
        }

        loginBtn.setOnClickListener{
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if(email.isNullOrBlank() || password.isNullOrBlank()){
                Toast.makeText(this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show()
            }
            else{

                //Sign in with Google Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Benvenuto in Runmate!", Toast.LENGTH_SHORT).show()
                        // load analytics and set user properties
                        val analytics = FirebaseAnalytics.getInstance(applicationContext)
                        setUserProperties(applicationContext, analytics)
                        loadMainActivity()

                    } else {
                        Toast.makeText(baseContext, "Email o password non corretti", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

        }
    }

    /*
    load main activity. Annotation is used to trace the performance of the funciton call,
    including nested functions.
     */
    @AddTrace(name = "loadMainFromLoginTrace", enabled = true)
    private fun loadMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}