package com.example.myapplication.util.webrtc

import android.app.Application
import com.example.myapplication.util.mediapipe.GestureRecogObj
//import com.example.myapplication.util.mediapipe.GestureRecognitionSingleton
import com.example.myapplication.util.network.RetrofitClient
import com.example.myapplication.util.network.TokenManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // TokenManager 생성 및 RetrofitClient 초기화
        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.initialize(tokenManager)
        //GestureRecognitionSingleton.initialize(applicationContext)
        GestureRecogObj.initialize(applicationContext)
    }
}