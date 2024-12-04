package com.example.myapplication.util.dataClass

data class VerificationRequest(
    val phoneNumber: String,
    val code: String,
    val name: String,
    val birthYear: Int
)
