package com.example.myapplication.util

class MusicRepository(private val apiService: ApiService) {
    suspend fun fetchBeats(musicId: Int):List<Float>{
        return apiService.getBeats(musicId)
    }
    suspend fun fetchMusics(): List<Music> {
        return apiService.getMusicList()
    }
}