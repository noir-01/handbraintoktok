package com.example.myapplication.util

import com.example.myapplication.util.dataClass.Music
import com.example.myapplication.util.network.ApiService

class MusicRepository(private val apiService: ApiService) {
    suspend fun fetchBeats(musicId: Int):List<Float>{
        return apiService.getBeats(musicId)
    }
    suspend fun fetchMusics(): List<Music> {
        return apiService.getMusicList()
    }
}