package com.example.myapplication.util.network

import com.example.myapplication.util.dataClass.Music
import com.example.myapplication.util.dataClass.RandomGameHistoryDto
import com.example.myapplication.util.dataClass.RhythmGameHistoryDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/music/getMusicList")
    suspend fun  getMusicList(): List<Music>

    @GET("/music/{musicId}/getBeatList")
    suspend fun getBeats(@Path("musicId") songId: Int): List<Float>

    @GET("/music/download/{musicId}")
    suspend fun downloadMusic(@Path("musicId") songId: Int): ResponseBody

    @GET("/history/rhythm/get/{musicId}")
    suspend fun getRhythmRank(@Path("musicId") musicId: Int): List<RhythmGameHistoryDto>

    @GET("/history/random/get")
    suspend fun getRandomHistory(
        @Query("gameType") gameType:String,
        @Query("period") period:String
    ): List<RandomGameHistoryDto>
}

fun durationToSec(duration: String): Int {
    val parts = duration.split(":")
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val seconds = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return hours * 3600 + minutes * 60 + seconds
}