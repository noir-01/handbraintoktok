package com.example.myapplication.util

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/music/getMusicList")
    suspend fun  getMusicList(): List<Music>

    @GET("/music/{musicId}/getBeatList")
    suspend fun getBeats(@Path("musicId") songId: Int): List<Float>
}
data class Music(
    val id: Int,
    val title: String,
    val artist: String,
    val duration: String,
    val filePath: String?=null
)