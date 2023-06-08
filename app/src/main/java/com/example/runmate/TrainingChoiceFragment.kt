package com.example.runmate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.google.firebase.analytics.FirebaseAnalytics

class TrainingChoiceFragment : Fragment() {

    val bundle = Bundle()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        val view = inflater.inflate(R.layout.fragment_training_choice, container, false)

        val btn_walk = view.findViewById<ImageButton>(R.id.btn_walk)
        btn_walk.setOnClickListener {
            startTrainingFragment("Camminata")
            logTrainingEvent("camminata")
        }

        val btn_run = view.findViewById<ImageButton>(R.id.btn_run)
        btn_run.setOnClickListener {
            startTrainingFragment("Corsa")
            logTrainingEvent("Corsa")
        }

        return view
    }

    private fun startTrainingFragment(type: String){
        val fragment = TrainingFragment()
        bundle.putString("trainingType", type)
        fragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment, fragment, "TrainingFragment")
        //transaction.addToBackStack(null) // add the transaction to the back stack to enable back navigation
        transaction.commit()
    }

    private fun logTrainingEvent(trainingType: String) {
        val analytics = FirebaseAnalytics.getInstance(requireContext())
        val params = Bundle()
        params.putString("trainingType", trainingType)
        analytics.logEvent("training_selected", params)
    }

}