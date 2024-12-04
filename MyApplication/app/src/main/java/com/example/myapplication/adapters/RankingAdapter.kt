package com.example.myapplication.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.dataClass.Rank

class RankingAdapter(
    private var rankList: List<Rank>,
    private var myRank: Int) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    inner class RankingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recyclerView: View  = view.findViewById(R.id.recyclerView)
        val rankingTextView: TextView = view.findViewById(R.id.rankPosition)
        val nameTextView: TextView = view.findViewById(R.id.rankName)
        val scoreTextView: TextView = view.findViewById(R.id.rankScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        Log.d("myRank","$myRank")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rank, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        var rank = rankList[position]
        holder.rankingTextView.text = "${rank.ranking}"
        holder.nameTextView.text = rank.name
        holder.scoreTextView.text = "${rank.score}점"
        if (position == myRank-1) {
            holder.recyclerView.setBackgroundResource(R.drawable.my_rank_background)
        }else{
            holder.itemView.setBackgroundResource(0)
        }
    }

    fun updateData(newRankingList: List<Rank>,newRank: Int) {
        rankList = newRankingList
        myRank = newRank
        notifyDataSetChanged() // 데이터 변경 알림
    }

    override fun getItemCount(): Int {
        return rankList.size
    }
}
