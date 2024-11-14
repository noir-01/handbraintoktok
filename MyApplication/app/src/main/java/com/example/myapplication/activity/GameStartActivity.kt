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
import android.util.Size
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.util.GestureRecognition
import com.example.myapplication.util.HandLandMarkHelper
import com.example.myapplication.util.ResourceUtils.imageResources
import com.example.myapplication.util.WebSocketClient
import com.example.myapplication.util.gestureLabels
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameStartActivity : BaseActivity(),WebSocketClient.WebSocketCallback {
    private lateinit var previewView: PreviewView
    private val CAMERA_REQUEST_CODE = 1001

    private lateinit var countdownImageView: ImageView
    private lateinit var startImageView: ImageView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var gestureRecognition: GestureRecognition
    private lateinit var handLandmarkerHelper: HandLandMarkHelper
    private lateinit var webSocketClient: WebSocketClient

    //websocket interface
    override fun onMessageReceived(message: String) {
        if (message.startsWith("next:")) {
            val problemInfo = message.substringAfter("next:")
            runOnUiThread {
                handleNextProblem(problemInfo)
            }
        }else if (message.startsWith("end")) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_start)

        // WebSocket
        val serverDomain = getString(R.string.server_domain)
        webSocketClient = WebSocketClient("wss://$serverDomain/ws", this)
        webSocketClient.connect()
        webSocketClient.sendMessage("abcde,false")
        
        countdownImageView = findViewById(R.id.countdownImageView)
        startImageView = findViewById(R.id.startImageView)

        val backButton = findViewById<ImageButton>(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
        countdownImageView.visibility = View.GONE
        startImageView.visibility = View.GONE

        showPopup()

        previewView = findViewById(R.id.camera_previewView)

        if (allPermissionsGranted()) {
            initializeMediaPipe()
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

                    // playCountdownSound()를 처음 한 번만 재생하도록 조건 추가
                    if (currentIndex == 0) {
                        playCountdownSound()
                    }

                    currentIndex++
                    handler.postDelayed(this, 1000)
                } else {
                    countdownImageView.visibility = View.GONE
                    startImageView.setImageResource(R.drawable.start)
                    startImageView.visibility = View.VISIBLE

                    handler.postDelayed({ startImageView.visibility = View.GONE }, 1000)
                }
            }
        }
        handler.post(countdownRunnable)
    }

    private fun playCountdownSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.start)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            it.release()
            mediaPlayer = null
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

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

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initializeMediaPipe() {
        lifecycleScope.launch(Dispatchers.Default) {
            gestureRecognition = GestureRecognition(this@GameStartActivity)
            handLandmarkerHelper = HandLandMarkHelper(
                context = this@GameStartActivity,
                runningMode = RunningMode.LIVE_STREAM,
                handLandmarkerHelperListener = object : HandLandMarkHelper.LandmarkerListener {
                    override fun onError(error: String, errorCode: Int) {
                        Log.e("HandActivity", "Hand Landmarker error: $error")
                    }

                    override fun onResults(resultBundle: HandLandMarkHelper.ResultBundle) {
                        val inferenceTime = resultBundle.inferenceTime
                        val height = resultBundle.inputImageHeight
                        val width = resultBundle.inputImageWidth
                        Log.d("HandActivity", "time: $inferenceTime, resol: $width*$height")

                        val predictedIndices = mutableListOf<Int>()

                        for (result in resultBundle.results) {
                            if (result.landmarks().isNotEmpty()) {
                                var leftHandIndex: Int? = null
                                var rightHandIndex: Int? = null

                                for (idx in result.landmarks().indices) {
                                    val handedness = result.handedness()[idx][0]
                                    val predictedIndex = gestureRecognition.predictByResult(result, idx)
                                    if (predictedIndex >= 0 && predictedIndex <= gestureLabels.size) {
                                        Log.d("HandActivity", "Predicted index: " + gestureLabels[predictedIndex])
                                        if (handedness.categoryName() == "Left") {
                                            leftHandIndex = predictedIndex
                                        } else if (handedness.categoryName() == "Right") {
                                            rightHandIndex = predictedIndex
                                        }
                                        leftHandIndex?.let { predictedIndices.add(it) }
                                        rightHandIndex?.let { predictedIndices.add(it) }
                                    }
                                }
                            } else {
                                Log.d("HandActivity", "No hand detected")
                            }
                        }
                        val message = when (predictedIndices.size) {
                            1 -> "${predictedIndices[0]},null"
                            else -> predictedIndices.joinToString(",")
                        }
                        if (predictedIndices.isNotEmpty()) {
                            webSocketClient.sendMessage(message)
                        }
                    }
                }
            )
            withContext(Dispatchers.Main) {
                startCamera()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val imageAnalyzer = ImageAnalysis.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                Size(640, 640),  // Target a lower resolution
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                            )
                        )
                    .build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                handLandmarkerHelper.detectLiveStream(
                    imageProxy = imageProxy,
                    isFrontCamera = true
                )
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "카메라 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("CameraPreview", "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleNextProblem(problemInfo: String) {
        val problemNumbers = problemInfo.split(",").map { it.trim().toIntOrNull() }

        val gameImageLeftView = findViewById<ImageView>(R.id.gameImageLeftView)
        val gameImageCenterView = findViewById<ImageView>(R.id.gameImageCenterView)
        val gameImageRightView = findViewById<ImageView>(R.id.gameImageRightView)
        when(val gameType = problemNumbers.getOrNull(0)) {
            // 따르기
            0 -> {
                val num1 = problemNumbers.getOrNull(1)
                val num2 = problemNumbers.getOrNull(2)

                // num1이나 num2가 2일 때 중앙 이미지를 표시
                if (num1 == 2 || num2 == 2) {
                    gameImageLeftView.visibility = View.GONE
                    gameImageRightView.visibility = View.GONE
                    gameImageCenterView.visibility = View.VISIBLE
                    gameImageCenterView.setImageResource(imageResources["hand_" + gestureLabels[2]]!!)
                } else {
                    // num1, num2가 모두 null이 아니면 좌우 이미지를 표시
                    gameImageLeftView.visibility = View.VISIBLE
                    gameImageRightView.visibility = View.VISIBLE
                    gameImageCenterView.visibility = View.GONE

                    num1?.let {
                        gameImageLeftView.setImageResource(imageResources["hand_" + gestureLabels[it] + "_l"]!!)
                    }
                    num2?.let {
                        gameImageRightView.setImageResource(imageResources["hand_" + gestureLabels[it] + "_r"]!!)
                    }
                }
            }
        }
        problemNumbers.forEachIndexed { index, num ->
            if (num == 2) {
                // num == 2일 때: 중앙 이미지 표시, 좌우 이미지는 숨김
                gameImageLeftView.visibility = View.GONE
                gameImageRightView.visibility = View.GONE
                gameImageCenterView.visibility = View.VISIBLE
                gameImageCenterView.setImageResource(imageResources["hand_" + gestureLabels[num]]!!)
            } else if (num != null) {
                // 일반 경우: 좌우 이미지 표시, 중앙 이미지는 숨김
                gameImageLeftView.visibility = View.VISIBLE
                gameImageRightView.visibility = View.VISIBLE
                gameImageCenterView.visibility = View.GONE

                if (index % 2 == 0) {
                    gameImageRightView.setImageResource(imageResources["hand_" + gestureLabels[num] + "_r"]!!)
                } else {
                    gameImageLeftView.setImageResource(imageResources["hand_" + gestureLabels[num] + "_l"]!!)
                }
            }
        }
    }
}
