package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.dataClass.UserScore

class LeaderboardAdapter(private var scores: List<UserScore>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    // ViewHolder 정의
    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.rankText)
        val nameText: TextView = view.findViewById(R.id.nameText)
        val scoreText: TextView = view.findViewById(R.id.scoreText)
        val comboText: TextView = view.findViewById(R.id.comboText)
    }

    class LeaderboardDiffCallback(
        private val oldList: List<UserScore>,
        private val newList: List<UserScore>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // 각 아이템의 고유 식별자를 비교 (예: ID)
            val sameItem = oldList[oldItemPosition].id == newList[newItemPosition].id
            Log.d("DiffUtil", "areItemsTheSame: $sameItem")
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // 아이템의 내용이 동일한지 비교
            val sameContent = oldList[oldItemPosition] == newList[newItemPosition]
            Log.d("DiffUtil", "areContentsTheSame: $sameContent")
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    fun updateList(newList: List<UserScore>) {
        val diffCallback = LeaderboardDiffCallback(scores, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        scores = newList
        Log.d("fuck","Moved")
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_item, parent, false)
        return LeaderboardViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val userScore = scores[position]
        holder.rankText.text = "${userScore.rank}위"
        holder.nameText.text = userScore.name
        holder.scoreText.text = "${userScore.score}점"
        holder.comboText.text = "🔥 콤보: ${userScore.combo}"

        Log.d("show","show")
        // 본인의 항목 강조
        if (userScore.name == "You") {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFD700")) // 금색 배경
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = scores.size
}
