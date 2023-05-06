package com.example.runmate

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Registration : Activity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirmPassword: EditText
    private lateinit var register_btn: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = Firebase.auth
        editEmail = findViewById<EditText>(R.id.email)
        editPassword = findViewById<EditText>(R.id.password)
        editConfirmPassword = findViewById<EditText>(R.id.confirm_password)
        register_btn = findViewById(R.id.btn_register)

        register_btn.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            if (email.isNullOrBlank()) {
                Toast.makeText(this, "Inserisci email", Toast.LENGTH_SHORT).show()
            }
            if (password.isNullOrBlank()) {
                Toast.makeText(this, "Inserisci password", Toast.LENGTH_SHORT).show()
            }
            if (password.isNullOrBlank()) {
                Toast.makeText(this, "Conferma la password", Toast.LENGTH_SHORT).show()
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "La password non corrisponde", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email.toString(), password.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser

                    } else {
                        Toast.makeText(
                            baseContext,
                            "registration failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
}