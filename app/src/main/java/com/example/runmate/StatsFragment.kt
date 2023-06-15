package com.example.runmate

import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.roundToInt

class StatsFragment:Fragment(R.layout.fragment_stats) {
    private lateinit var pb_steps: ProgressBar
    private lateinit var pb_distance: ProgressBar
    private lateinit var pb_calories: ProgressBar
    private lateinit var tv_steps_progress: TextView
    private lateinit var tv_distance_progress: TextView
    private lateinit var tv_calories_progress: TextView
    private lateinit var rv_activities: RecyclerView
    private lateinit var analytics: FirebaseAnalytics

    private var stepsGoal = 10000
    private var distanceGoal = 8000
    private var caloriesGoal = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val sharedPref = requireContext().getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)

        analytics = FirebaseAnalytics.getInstance(requireContext())

        trackFragment(analytics, "Statistics Fragment")

        pb_steps = view.findViewById(R.id.pb_steps_foreground)
        pb_distance = view.findViewById(R.id.pb_distance_foreground)
        pb_calories = view.findViewById(R.id.pb_calories_foreground)
        tv_steps_progress = view.findViewById(R.id.tv_steps_stats)
        tv_distance_progress = view.findViewById(R.id.tv_distance_stats)
        tv_calories_progress = view.findViewById(R.id.tv_calories_stats)
        rv_activities = view.findViewById(R.id.rv_activities)

        stepsGoal = sharedPref.getInt("Steps", 10000)
        distanceGoal = sharedPref.getInt("Meters", 8000)
        caloriesGoal = sharedPref.getInt("Calories", 1000)

        return view
    }

    override fun onResume() {
        super.onResume()

        // take stats values from local save and update stats UI
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val sharedPref = requireContext().getSharedPreferences("${uid}UserPrefs", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            //val layoutManager = LinearLayoutManager(requireContext())
            val adapter: TrainingAdapter

            val json = sharedPref.getString("trainingList", null)
            if (json != null) {
                val gson = Gson()
                val listType = object : TypeToken<MutableList<TrainingObject>>() {}.type
                val pastTraining = gson.fromJson<MutableList<TrainingObject>>(json, listType)

                // pass training list to show trainings in the UI
                adapter = TrainingAdapter(pastTraining)
            }
            else{
                // pass empty list because there are no previous trainings
                adapter = TrainingAdapter(listOf())
            }
            rv_activities.layoutManager = LinearLayoutManager(requireContext())
            rv_activities.adapter = adapter

            // update text view and circular progress bars
            updateStatsUI(sharedPref.getInt("totalSteps", 0), sharedPref.getFloat("totalDistance", 0f), sharedPref.getFloat("totalCalories", 0f))
            updateTVColor(tv_steps_progress, ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.steps)))
            updateTVColor(tv_distance_progress, ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.distance)))
            updateTVColor(tv_calories_progress, ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.calories)))
        }
    }

    // Updates text views with locally saved stats and colors circular progress bars
    private fun updateStatsUI(steps: Int, distance: Float, calories: Float) {
        pb_steps.progress = (steps.toFloat() / stepsGoal.toFloat() * 100).toInt()
        pb_distance.progress = (distance / distanceGoal.toFloat() * 100).toInt()
        pb_calories.progress = (calories / caloriesGoal.toFloat() * 100).toInt()
        tv_steps_progress.text = "$steps / $stepsGoal passi"
        tv_distance_progress.text = "${distance.roundToInt()} / $distanceGoal m"
        tv_calories_progress.text = "${calories.roundToInt()} / $caloriesGoal kcal"
    }

    // Changes the color of part of the stats text views
    private fun updateTVColor(tv: TextView, color: ForegroundColorSpan) {
        val txt = tv.text
        val spannableString = SpannableString(txt)
        spannableString.setSpan(color, 0, txt.indexOf("/"), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tv.text = spannableString
    }

    private fun trackFragment(analytics : FirebaseAnalytics, fragName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, fragName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "StatsFragment")
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}