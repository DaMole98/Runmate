package com.example.runmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class TrainingAdapter(private val dataList: List<TrainingObject>) : RecyclerView.Adapter<TrainingAdapter.TrainingItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingItemHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.training_item, parent, false)
        return TrainingItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrainingItemHolder, position: Int) {
        if (dataList.isEmpty()) {
            holder.showEmptyState()
        } else {
            val itemData = dataList[position]
            holder.bind(itemData)
        }
    }

    override fun getItemCount(): Int {
        return if (dataList.isEmpty()) 1 else dataList.size
    }

    inner class TrainingItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv_act_sum_1: TextView = itemView.findViewById(R.id.tv_activity_summary_1)
        private val tv_act_sum_2: TextView = itemView.findViewById(R.id.tv_activity_summary_2)
        private val imgv_activity: ImageView = itemView.findViewById(R.id.imgv_activity_icon)

        fun bind(itemData: TrainingObject) {
            tv_act_sum_1.text = "${itemData.name} | ${itemData.date} | ${itemData.startTime}"
            //tv_act_sum_2.text = "${itemData.steps} passi | ${itemData.distance} m |" + String.format(" %.${3}f kcal", itemData.calories) + " | ${itemData.duration}"
            tv_act_sum_2.text = "${itemData.steps} passi | ${itemData.distance} m | ${itemData.calories.roundToInt()} kcal" + " | ${itemData.duration}"

            if (itemData.name == "Camminata") imgv_activity.setImageResource(R.drawable.walk)
            else imgv_activity.setImageResource(R.drawable.run)
        }

        fun showEmptyState() {
            val tv_act_empty: TextView = itemView.findViewById(R.id.tv_activity_empty)

            imgv_activity.visibility = View.INVISIBLE
            tv_act_empty.visibility = View.VISIBLE
            tv_act_sum_1.visibility = View.INVISIBLE
            tv_act_sum_2.visibility = View.INVISIBLE
        }
    }
}