package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ViewHolder.MusicViewHolder
import com.example.myapplication.util.dataClass.Music

// Adapter 클래스
class MusicAdapter(
    private var musics: List<Music>,
    private val onMusicSelected: (Music) -> Unit,
) : RecyclerView.Adapter<MusicViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION // 선택된 아이템 위치를 저장하는 변수

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(itemView, musics, onMusicSelected)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val song = musics[position]
        val songDetails = "${song.title} - ${song.artist} - ${song.duration}"

        holder.songButton.text = songDetails

        // 선택된 버튼에 대해 색 변경
        if (holder.bindingAdapterPosition == selectedPosition) {
            // 선택된 버튼의 배경색을 진하게 설정
            holder.songButton.setBackgroundResource(R.drawable.btn_item_music_selected)
            holder.songButton.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white)) // 텍스트 색 변경 (선택된 상태)
        } else {
            // 선택되지 않은 버튼은 기본 색상으로 설정
            holder.songButton.setBackgroundResource(R.drawable.btn_item_music)
            holder.songButton.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black)) // 기본 텍스트 색
        }

        // 버튼 클릭 리스너 설정
        holder.songButton.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val previousPosition = selectedPosition
            selectedPosition = adapterPosition
            ////선택된 버튼을 맨 위쪽으로 보이게 스크롤하는 ㅋ드
            //val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            //layoutManager.scrollToPositionWithOffset(selectedPosition , 0)
            // RecyclerView 업데이트
            notifyItemChanged(previousPosition) // 이전 선택된 아이템 업데이트
            notifyItemChanged(selectedPosition) // 현재 선택된 아이템 업데이트
            onMusicSelected(song)

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
