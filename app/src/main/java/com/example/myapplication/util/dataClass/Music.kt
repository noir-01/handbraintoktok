package com.example.myapplication.util.dataClass

data class Music(
    val id: Int,
    val title: String,
    val artist: String,
    val duration: String,
    val filePath: String?=null
)
