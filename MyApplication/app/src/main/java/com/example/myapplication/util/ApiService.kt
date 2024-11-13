package com.example.myapplication.util

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/music/getMusicList")
    suspend fun  getMusicList(): List<Music>

    @GET("/music/{musicId}/getBeatList")
    suspend fun getBeats(@Path("musicId") songId: Int): List<Float>

    @GET("/music/download/{musicId}")
    suspend fun downloadMusic(@Path("musicId") songId: Int): ResponseBody
}
data class Music(
    val id: Int,
    val title: String,
    val artist: String,
    val duration: String,
    val filePath: String?=null
)

fun durationToSec(duration: String): Int {
    val parts = duration.split(":")
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val seconds = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return hours * 3600 + minutes * 60 + seconds
}