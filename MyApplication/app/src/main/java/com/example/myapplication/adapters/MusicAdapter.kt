package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.Music

// ViewHolder 클래스
class SongViewHolder(itemView: View, private val musics: List<Music>, private val onMusicSelected: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
    val songButton: AppCompatButton = itemView.findViewById(R.id.songButton)
    init {
        // 버튼 클릭 시 콜백 호출
        songButton.setOnClickListener {
            val musicId = musics[bindingAdapterPosition].id
            onMusicSelected(musicId) // 콜백을 통해 음악 ID 전달
        }
    }
}

// Adapter 클래스
class MusicAdapter(
    private var musics: List<Music>,
    private val onMusicSelected: (Int) -> Unit
) : RecyclerView.Adapter<SongViewHolder>() {


    private var selectedPosition: Int = RecyclerView.NO_POSITION // 선택된 아이템 위치를 저장하는 변수

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return SongViewHolder(itemView,musics,onMusicSelected)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = musics[position]
        val songDetails = "${song.title} - ${song.artist} - ${song.duration}"

        // 버튼 텍스트 설정
        holder.songButton.text = songDetails

        // 선택된 버튼에 대해 색 변경
        if (holder.bindingAdapterPosition == selectedPosition) {
            // 선택된 버튼의 배경색을 진하게 설정
            holder.songButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.dark_gray))
            holder.songButton.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white)) // 텍스트 색 변경 (선택된 상태)
        } else {
            // 선택되지 않은 버튼은 기본 색상으로 설정
            holder.songButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
            holder.songButton.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black)) // 기본 텍스트 색
        }

        // 버튼 클릭 리스너 설정
        holder.songButton.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val previousPosition = selectedPosition
            selectedPosition = adapterPosition

            // RecyclerView 업데이트
            notifyItemChanged(previousPosition) // 이전 선택된 아이템 업데이트
            notifyItemChanged(selectedPosition) // 현재 선택된 아이템 업데이트
            onMusicSelected(song.id)
        }
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
