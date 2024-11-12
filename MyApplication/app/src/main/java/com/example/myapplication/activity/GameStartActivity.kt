package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class GameStartActivity : BaseActivity() {
    private lateinit var previewView: PreviewView
    private val CAMERA_REQUEST_CODE = 1001

    private lateinit var countdownImageView: ImageView
    private lateinit var startImageView: ImageView
    private var mediaPlayer: MediaPlayer? = null  // Use a nullable MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_start)

        countdownImageView = findViewById(R.id.countdownImageView)
        startImageView = findViewById(R.id.startImageView)

        countdownImageView.visibility = View.GONE
        startImageView.visibility = View.GONE

        showPopup()

        previewView = findViewById(R.id.camera_previewView)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
            )
        }
    }

    private fun showPopup() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Game Start")
            .setMessage("핸드폰을 흔들리지 않게 세워주세요.")
            .setPositiveButton("확인") { _, _ ->
                startCountdown()
            }
            .create()
        alertDialog.setOnShowListener {
            val positiveButton: Button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val params = positiveButton.layoutParams as LinearLayout.LayoutParams
            params.width = LinearLayout.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.CENTER
            positiveButton.layoutParams = params
        }
        alertDialog.show()
    }

    private fun startCountdown() {
        countdownImageView.visibility = View.VISIBLE

        val countdownImages = arrayOf(
            R.drawable.three,
            R.drawable.two,
            R.drawable.one
        )
        val handler = Handler(Looper.getMainLooper())
        var currentIndex = 0

        val countdownRunnable = object : Runnable {
            override fun run() {
                if (currentIndex < countdownImages.size) {
                    countdownImageView.setImageResource(countdownImages[currentIndex])
                    countdownImageView.visibility = View.VISIBLE

                    playCountdownSound()

                    currentIndex++
                    handler.postDelayed(this, 1000)
                } else {
                    countdownImageView.visibility = View.GONE
                    startImageView.setImageResource(R.drawable.start)
                    startImageView.visibility = View.VISIBLE

                    playCountdownSound()

                    handler.postDelayed({ startImageView.visibility = View.GONE }, 1000)
                }
            }
        }
        handler.post(countdownRunnable)
    }

    private fun playCountdownSound() {
        // Release the existing MediaPlayer before creating a new one
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.start)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            it.release()  // Release resources after sound completes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()  // Release MediaPlayer resources when activity is destroyed
        mediaPlayer = null
    }

    // Handle camera permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Check if all required permissions are granted
    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Start the camera preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "카메라 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("CameraPreview", "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
}
