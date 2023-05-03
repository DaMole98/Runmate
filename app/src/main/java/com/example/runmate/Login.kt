package com.example.runmate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var login_btn: Button
    private lateinit var register_btn: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var textView: TextView


    override fun onStart(){
        super.onStart()
        val currentUser = mAuth.currentUser
        if(currentUser != null){
            recreate()
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

            if(email.isNullOrBlank()){
                Toast.makeText(this, "Inserisci email", Toast.LENGTH_SHORT).show()
            }
            if(password.isNullOrBlank()){
                Toast.makeText(this, "Inserisci password", Toast.LENGTH_SHORT).show()
            }

            mAuth.signInWithEmailAndPassword(email.toString(), password.toString()).addOnCompleteListener(this){ task ->
                if(task.isSuccessful) {
                    val user = mAuth.currentUser
                    Toast.makeText(baseContext, "Authentication Successful", Toast.LENGTH_SHORT).show()
                    intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                }
                else {
                    Toast.makeText(baseContext, "Authentication Failed", Toast.LENGTH_SHORT).show()
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