package com.example.runmate

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment

class StatsFragment:Fragment(R.layout.fragment_stats) {
    private lateinit var pb_steps: ProgressBar
    private lateinit var pb_distance: ProgressBar
    private lateinit var pb_calories: ProgressBar
    private lateinit var tv_steps_progress: TextView
    private lateinit var tv_distance_progress: TextView
    private lateinit var tv_calories_progress: TextView

    // TODO(get these values from the database or from something else)
    private var stepsGoal = 10000
    private var distanceGoal = 8000
    private var caloriesGoal = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        pb_steps = view.findViewById(R.id.pb_steps_foreground)
        pb_distance = view.findViewById(R.id.pb_distance_foreground)
        pb_calories = view.findViewById(R.id.pb_calories_foreground)
        tv_steps_progress = view.findViewById(R.id.tv_steps_stats)
        tv_distance_progress = view.findViewById(R.id.tv_distance_stats)
        tv_calories_progress = view.findViewById(R.id.tv_calories_stats)

        // TODO((example call. Current parameters are just for example and use "SharedPreferences"). This function should be called from CaloriesService or TrainingFragment with real values)
        val sharedPref = context?.getSharedPreferences("TRAINING_DATA", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            updateStatsUI(sharedPref.getInt("totalSteps", 0), sharedPref.getInt("totalDistance", 0), sharedPref.getFloat("totalCalories", 0f))
        }

        return view
    }

    private fun updateStatsUI(steps: Int, distance: Int, calories: Float){
        pb_steps.progress = (steps.toFloat() / stepsGoal.toFloat() * 100).toInt()
        pb_distance.progress = (distance.toFloat() / distanceGoal.toFloat() * 100).toInt()
        pb_calories.progress = (calories / caloriesGoal.toFloat() * 100).toInt()
        tv_steps_progress.text = "$steps / $stepsGoal passi"
        tv_distance_progress.text = "$distance / $distanceGoal m"
        tv_calories_progress.text = "${calories.toInt()} / $caloriesGoal kcal"
    }
}