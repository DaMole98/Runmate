package com.example.runmate

import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserFragment: Fragment(R.layout.fragment_user) {
    private lateinit var logoutBtn: AppCompatImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        logoutBtn = view.findViewById<AppCompatImageButton>(R.id.btn_logout)

        logoutBtn.setOnClickListener{

            Firebase.auth.signOut()
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            activity?.finish()
        }


        return view
    }
}