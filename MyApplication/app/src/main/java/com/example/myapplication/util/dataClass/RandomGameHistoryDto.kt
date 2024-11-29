package com.example.myapplication.util.dataClass

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class RandomGameHistoryDto(
    @SerializedName("startDate")
    val startDate: String,  // This will be returned as a String
    val averageReactionTime: Float
) {
    // Custom function to parse the startDate string into a LocalDate object
    fun getStartDateAsLocalDate(): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.parse(startDate, formatter)
    }
}