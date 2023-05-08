package com.example.runmate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var login_btn: Button
    private lateinit var register_btn: Button
    private lateinit var mAuth: FirebaseAuth


   override fun onStart(){
        super.onStart()
        val currentUser = mAuth.currentUser
        if(currentUser != null){
            intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginscreen)

        mAuth = Firebase.auth
        //textView = findViewById(R.id.login_prompt)
        editEmail = findViewById<EditText>(R.id.email)
        editPassword = findViewById<EditText>(R.id.password)
        login_btn = findViewById(R.id.btn_login)


        login_btn.setOnClickListener{
            val email = editEmail.text
            val password = editPassword.text

            if (email.isNullOrBlank()) {
                Toast.makeText(this, "Inserisci e-mail", Toast.LENGTH_SHORT).show()
            }
            if(password.isNullOrBlank()){
                Toast.makeText(this, "Inserisci password", Toast.LENGTH_SHORT).show()
            }

            else{
                mAuth.signInWithEmailAndPassword(email.toString(), password.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Accesso completato", Toast.LENGTH_SHORT)
                        .show()
                    intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(baseContext, "Accesso fallito", Toast.LENGTH_SHORT).show()
                }
            }
            }

        }


        register_btn = findViewById(R.id.btn_register)
        register_btn.setOnClickListener{ view ->
            val intent = Intent(view.context, Registration::class.java)
            startActivity(intent)
            finish()
        }

    }
}