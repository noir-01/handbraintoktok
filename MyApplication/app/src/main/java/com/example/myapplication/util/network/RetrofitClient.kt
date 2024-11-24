package com.example.myapplication.util.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var tokenManager: TokenManager? = null

    fun initialize(tokenManager: TokenManager) {
        RetrofitClient.tokenManager = tokenManager
    }

    private val client by lazy {
        requireNotNull(tokenManager) { "TokenManager must be initialized before using RetrofitClient" }

        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager!!))
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://handbraintoktok.duckdns.org:8080")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun getTokenManager(): TokenManager {
        return tokenManager ?: throw IllegalStateException("TokenManager is not initialized.")
    }
}