package com.example.runmate

interface TrainingFragmentCallback {
    fun updateUI(steps: Int, distance: Int, calories: Float)
    fun getTrainingTime(): String
}