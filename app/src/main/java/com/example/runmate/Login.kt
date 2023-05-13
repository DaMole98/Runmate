package com.example.runmate


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    @AddTrace(name = "OnStartLoginTrace, enabled = true")
    override fun onStart(){
        super.onStart()
        val currentUser = mAuth.currentUser
        if(currentUser != null){
            currentUser.reload()
            //val name = currentUser.displayName
            Toast.makeText(baseContext, "Benvenuto", Toast.LENGTH_LONG).show()
            intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()

        }
    }

    @AddTrace(name = "OnCreateLoginTrace, enabled = true")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginscreen)

        mAuth = Firebase.auth
        //textView = findViewById(R.id.login_prompt)
        editEmail = findViewById<EditText>(R.id.email)
        editPassword = findViewById<EditText>(R.id.password)
        loginBtn = findViewById(R.id.btn_login)


        loginBtn.setOnClickListener{
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if(email.isNullOrBlank() || password.isNullOrBlank()){
                Toast.makeText(this, "Inserisci tutti i campi", Toast.LENGTH_SHORT).show()
            }
            else{

                 mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                     if (task.isSuccessful) {
                         Toast.makeText(baseContext, "Benvenuto in Runmate!", Toast.LENGTH_SHORT).show()

                         intent = Intent(applicationContext, MainActivity::class.java)
                         startActivity(intent)
                         finish()

                     } else {
                         Toast.makeText(baseContext, "Email o password non corretti", Toast.LENGTH_LONG)
                             .show()
                     }
                }
            }

        }


        registerBtn = findViewById(R.id.btn_register)
        registerBtn.setOnClickListener{ view ->
            val intent = Intent(view.context, Registration::class.java)
            startActivity(intent)
            finish()
        }

    }
}