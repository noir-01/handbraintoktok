package com.example.myapplication.util.network

import okhttp3.Interceptor
import okhttp3.Response

// Retrofit Interceptor for automatically adding the token to requests
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        val originalRequest = chain.request()

        // If no token exists, proceed with the original request
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Add the token to the request header
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}