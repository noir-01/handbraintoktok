package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class InstallActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install)

        // Initialize views
        videoView = findViewById(R.id.videoView)
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)

        // Set up video URI with a try-catch block
        try {
            val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.how_to_make_phone_holder)
            videoView.setVideoURI(videoUri)
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            return // Exit early if video setup fails
        }

        // Play button functionality
        playButton.setOnClickListener {
            if (!videoView.isPlaying) {
                videoView.start()
                Toast.makeText(this, "Playing video", Toast.LENGTH_SHORT).show()
            }
        }

        // Pause button functionality
        pauseButton.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                Toast.makeText(this, "Video paused", Toast.LENGTH_SHORT).show()
            }
        }

        // Back button functionality
        val backButton = findViewById<ImageButton>(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause the video if the activity goes into the background
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.stopPlayback() // Release video resources on destroy
    }
}
