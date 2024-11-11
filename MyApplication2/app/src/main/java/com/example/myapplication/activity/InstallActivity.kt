package com.example.myapplication.activity
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class InstallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install)

        val videoView = findViewById<VideoView>(R.id.VideoView)
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.sample_video) // Use your video resource

        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            videoView.start()
        }
    }
}