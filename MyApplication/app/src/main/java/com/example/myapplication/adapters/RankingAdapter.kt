package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.Rank

class RankingAdapter(private val rankList: List<Rank>) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    inner class RankingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankingTextView: TextView = view.findViewById(R.id.rankPosition)
        val nameTextView: TextView = view.findViewById(R.id.rankName)
        val scoreTextView: TextView = view.findViewById(R.id.rankScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rank, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val rank = rankList[position]
        holder.rankingTextView.text = "#${rank.ranking}"
        holder.nameTextView.text = rank.name
        holder.scoreTextView.text = "${rank.score} points"
    }

    override fun getItemCount(): Int {
        return rankList.size
    }
}
