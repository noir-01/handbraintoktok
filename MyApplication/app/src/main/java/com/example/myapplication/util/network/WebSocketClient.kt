package com.example.myapplication.util.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response

class WebSocketClient(private val url: String, private val callback: WebSocketCallback) {
    // WebSocket 관련 필드
    private lateinit var webSocket: WebSocket
    private val client = OkHttpClient()

    private var isConnected = false

    interface WebSocketCallback {
        fun onMessageReceived(message: String)
    }

    fun connect() {
        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                isConnected=true
                Log.d("SocketWeb","connect")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.d("SocketWeb","recieved: $text")
                //UI 업데이트를 위해 callback
                callback.onMessageReceived(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                isConnected=false
                Log.d("SocketWeb","closed")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.d("SocketWeb","failed")
            }
        })
    }

    // 메시지 전송 메서드
    fun sendMessage(message: String) {
        if(isConnected) {
            webSocket.send(message)
        }else{
            Log.d("SocketWeb", "Failed sending message: $message")
        }
    }

    // 연결 종료 메서드
    fun disconnect() {
        webSocket.close(1000, "클라이언트 종료")
    }
}
