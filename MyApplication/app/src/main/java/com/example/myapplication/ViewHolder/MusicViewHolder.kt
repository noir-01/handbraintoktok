package com.example.myapplication.ViewHolder

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.Music

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
    fun bind(music: Music, isSelected: Boolean) {
        songButton.text = "${music.title} - ${music.artist} - ${music.duration}"

        val context = itemView.context
        if (isSelected) {
            songButton.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_gray))
            songButton.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            songButton.setBackgroundColor(ContextCompat.getColor(context, R.color.gray))
            songButton.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }
}