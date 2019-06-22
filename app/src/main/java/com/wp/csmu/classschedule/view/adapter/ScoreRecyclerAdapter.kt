package com.wp.csmu.classschedule.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wp.csmu.classschedule.R
import com.wp.csmu.classschedule.view.bean.Score

class ScoreRecyclerAdapter(private var data: List<Score>?, private val listener: OnClickListener) : RecyclerView.Adapter<ScoreRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.score_recyclerview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.subjectName.text = data!![position].name
        holder.score.text = data!![position].score
        try {
            if (data!![position].score.toDouble() < 60.0) holder.score.setTextColor(Color.RED)
        } catch (e: Exception) {
        }
        holder.itemView.setOnClickListener { v -> listener.onClick(v, holder.adapterPosition) }
    }

    override fun getItemCount(): Int {
        return if (data == null) 0 else data!!.size
    }

    fun updateData(newData: List<Score>) {
        this.data = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var subjectName: TextView = itemView.findViewById(R.id.scoreRecyclerItemTvSubject)
        var score: TextView = itemView.findViewById(R.id.scoreRecyclerItemTvScore)
    }

    interface OnClickListener {
        fun onClick(view: View, position: Int)
    }
}
