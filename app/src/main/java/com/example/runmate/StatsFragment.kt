package com.example.runmate

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
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

        return view
    }

    override fun onResume() {
        super.onResume()

        // take stats values from local save and update stats UI
        val sharedPref = context?.getSharedPreferences("TRAINING_DATA", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            updateStatsUI(sharedPref.getInt("totalSteps", 0), sharedPref.getInt("totalDistance", 0), sharedPref.getFloat("totalCalories", 0f))
            updateTVColor(tv_steps_progress, ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.steps)))
            updateTVColor(tv_distance_progress, ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.distance)))
            updateTVColor(tv_calories_progress, ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.calories)))
        }
    }

    private fun updateStatsUI(steps: Int, distance: Int, calories: Float) {
        pb_steps.progress = (steps.toFloat() / stepsGoal.toFloat() * 100).toInt()
        pb_distance.progress = (distance.toFloat() / distanceGoal.toFloat() * 100).toInt()
        pb_calories.progress = (calories / caloriesGoal.toFloat() * 100).toInt()
        tv_steps_progress.text = "$steps / $stepsGoal passi"
        tv_distance_progress.text = "$distance / $distanceGoal m"
        tv_calories_progress.text = "${calories.toInt()} / $caloriesGoal kcal"
    }

    // Changes the color of part of the statistics texts
    private fun updateTVColor(tv: TextView, color: ForegroundColorSpan) {
        val txt = tv.text
        val spannableString = SpannableString(txt)
        spannableString.setSpan(color, 0, txt.indexOf("/"), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tv.text = spannableString
    }
}