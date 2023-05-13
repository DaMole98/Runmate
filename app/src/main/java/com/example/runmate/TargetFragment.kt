package com.example.runmate

import android.os.Bundle
import android.text.TextUtils.replace
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class TargetFragment: Fragment(R.layout.fragment_target) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_target, container, false)

        val confirm_target_btn = view.findViewById<Button>(R.id.confirm_target_button)
        confirm_target_btn.setOnClickListener {

        }
        val reset_target_btn = view.findViewById<Button>(R.id.reset_target_button)
        return view
    }
}