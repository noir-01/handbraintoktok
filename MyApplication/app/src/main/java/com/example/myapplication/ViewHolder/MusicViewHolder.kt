package com.example.myapplication.ViewHolder

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.dataClass.Music

// ViewHolder 클래스
class MusicViewHolder(itemView: View, private val musics: List<Music>, private val onMusicSelected: (Music) -> Unit) : RecyclerView.ViewHolder(itemView) {
    val songButton: AppCompatButton = itemView.findViewById(R.id.songButton)
    init {
        // 버튼 클릭 시 콜백 호출
        songButton.setOnClickListener {
            val music = musics[bindingAdapterPosition]
            onMusicSelected(music) // 콜백을 통해 음악 ID 전달
        }
    }
}