package com.example.myapplication.util.dataClass

data class RhythmGameHistoryDto(
    val userDto: UserDto,
    val combo: Int,
    val score: Int,
    val difficulty: String,
    val date: String
)