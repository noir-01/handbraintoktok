package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.dataClass.UserScore

class LeaderboardAdapter(private val context: Context) :
    ListAdapter<UserScore, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardDiffCallback()) {
    private val myName: String = getMyName()

    // ViewHolder 정의
    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.rankText)
        val nameText: TextView = view.findViewById(R.id.nameText)
        val scoreText: TextView = view.findViewById(R.id.scoreText)
        val comboText: TextView = view.findViewById(R.id.comboText)
    }

    // DiffUtil을 사용한 비교
    class LeaderboardDiffCallback : DiffUtil.ItemCallback<UserScore>() {

        override fun areItemsTheSame(oldItem: UserScore, newItem: UserScore): Boolean {
            return oldItem.id == newItem.id  // 예: UserScore에서 id를 기준으로 비교
        }

        override fun areContentsTheSame(oldItem: UserScore, newItem: UserScore): Boolean {
            return oldItem == newItem  // 모든 필드가 동일한지 비교
        }
    }

    // onCreateViewHolder는 RecyclerView.Adapter에서 그대로 유지
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_item, parent, false)
        return LeaderboardViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val userScore = getItem(position)
        holder.rankText.text = "${userScore.rank}위"
        holder.nameText.text = userScore.name
        holder.scoreText.text = "${userScore.score}점"
        holder.comboText.text = "🔥 콤보: ${userScore.combo}"

        // 본인의 항목 강조
        if (userScore.name == myName) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFD700")) // 금색 배경
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }
    private fun getMyName():String{
        //RegisterActivity에서 저장했던 이름 꺼내오기
        val sharedPreferences = context.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val name = sharedPreferences.getString("userName","")
        return name?:""
    }
    // getItemCount는 ListAdapter에서 자동으로 처리
    // itemCount는 getItemCount()를 사용해 자동으로 처리됩니다.
}

