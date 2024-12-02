package com.example.myapplication.activity.game

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import android.Manifest
import com.example.myapplication.util.network.MyHttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class WebViewTestActivity : AppCompatActivity() {
    private lateinit var server :MyHttpServer
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_test)

        CoroutineScope(Dispatchers.IO).launch {
            server = MyHttpServer(this@WebViewTestActivity, 8080)
            server.start()
        }

        val webView: WebView = findViewById(R.id.webView)
        //webView.settings.allowContentAccess=true
        webView.settings.allowFileAccess=true
        webView.settings.allowContentAccess=true
        webView.settings.apply {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                Log.d("WebViewLog", message?.message() ?: "No message") // Log JavaScript console output
                return super.onConsoleMessage(message)
            }
        }
        // Create a JavaScript interface to pass the file path
        val musicFilePath = "http://localhost:8080/mp3/1.mp3"
        // Load HTML file

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {

                // You can call evaluateJavascript here, after the page has loaded
                CoroutineScope(Dispatchers.Main).launch {
                    super.onPageFinished(view, url)
                    webView.evaluateJavascript("loadAudio('$musicFilePath');",null)
                    delay(1000)
                    webView.evaluateJavascript("playAudio();", null)
                }
            }
        }
        webView.loadUrl("file:///android_asset/index2.html")
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
    }
}