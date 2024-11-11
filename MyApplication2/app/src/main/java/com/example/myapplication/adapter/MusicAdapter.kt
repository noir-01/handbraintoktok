package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.Music

class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val songButton: AppCompatButton = itemView.findViewById(R.id.songButton)
//    val titleTextView: TextView = itemView.findViewById(R.id.songTitle)
//    val artistTextView: TextView = itemView.findViewById(R.id.songArtist)
//    val durationTextView: TextView = itemView.findViewById(R.id.songDuration)
}

// Adapter 클래스
class MusicAdapter(private var musics: List<Music>) : RecyclerView.Adapter<SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return SongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = musics[position]
        val songDetails = "${song.title} - ${song.artist} - ${song.duration}"
        holder.songButton.text = songDetails
    }

    override fun getItemCount(): Int {
        return musics.size
    }

    // 데이터를 갱신할 수 있도록 updateSongs 메서드 추가
    fun updateSongs(newSongs: List<Music>) {
        musics = newSongs
        notifyDataSetChanged() // 데이터 갱신 후 RecyclerView에 변경 사항 반영
    }
}