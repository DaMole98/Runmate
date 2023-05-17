package com.example.runmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class UserFragment: Fragment(R.layout.fragment_user) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        val user_view = view.findViewById<TextView>(R.id.user_view)

        val username = arguments?.getString("USERNAME", "")

        user_view.text = "Ciao $username"

        val logoutBtn = view.findViewById<Button>(R.id.btn_logout)

        logoutBtn.setOnClickListener{
            Firebase.auth.signOut()
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            activity?.finish()
        }

        val targetBtn = view.findViewById<Button>(R.id.btn_target)
        targetBtn.setOnClickListener {
            val intent = Intent(requireContext(), TargetActivity::class.java)
            startActivity(intent)
        }

        return view
    }

}