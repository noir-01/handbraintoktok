package com.example.myapplication.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.Gravity
import android.widget.LinearLayout
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Button


import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy

import androidx.lifecycle.lifecycleScope
import com.google.mediapipe.tasks.vision.core.RunningMode

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

import com.example.myapplication.R
import com.example.myapplication.util.gestureLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.myapplication.util.WebSocketClient
import com.example.myapplication.util.GestureRecognition
import com.example.myapplication.util.HandLandMarkHelper
import com.example.myapplication.util.ResourceUtils.handImages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class GameStartActivity : BaseActivity(), WebSocketClient.WebSocketCallback {
    private lateinit var previewView: PreviewView
    private val CAMERA_REQUEST_CODE = 1001

    private lateinit var countdownImageView: ImageView
    private lateinit var startImageView: ImageView

    private lateinit var handLandmarkerHelper: HandLandMarkHelper
    private lateinit var gestureRecognition: GestureRecognition
    private lateinit var webSocketClient: WebSocketClient

    //websocket interface 구현
    override fun onMessageReceived(message: String) {
        if (message.startsWith("next:")) {
            val problemInfo = message.substringAfter("next:")
            runOnUiThread {
                // UI 업데이트 메서드 호출
                handleNextProblem(problemInfo)
            }
        }else if (message.startsWith("end")){
            CoroutineScope(Dispatchers.Main).launch {
                delay(500) // 3000ms = 3초
                finish() // 현재 Activity 종료, 이전 화면으로 돌아감
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_start)


        // WebSocket 연결
        val serverDomain = getString(R.string.server_domain)
        webSocketClient = WebSocketClient("wss://$serverDomain/ws", this)
        webSocketClient.connect()

        // 카메라 권한 체크 및 요청
        if (allPermissionsGranted()) {
            //첫 메시지: id + isRandomGame(지금은 true) 전송
            webSocketClient.sendMessage("abcde,false")
            initializeMediaPipe()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
            )
        }

        countdownImageView = findViewById(R.id.countdownImageView)
        startImageView = findViewById(R.id.startImageView)

        countdownImageView.visibility = View.GONE
        startImageView.visibility = View.GONE

        showPopup()

        // PreviewView 초기화
        previewView = findViewById(R.id.camera_previewView)

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
            val positiveButton:Button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            //"확인"을 중앙에 정렬하기 위해 LinearLayout.LayoutParams 설정
            val params = positiveButton.layoutParams as LinearLayout.LayoutParams
            params.width = LinearLayout.LayoutParams.MATCH_PARENT   //버튼의 너비를 부모 레이아웃의 너비로 설정
            params.gravity = Gravity.CENTER //버튼의 정렬을 중앙으로 설정
            positiveButton.layoutParams = params
        }
        alertDialog.show()
    }

    private fun startCountdown(){
        countdownImageView.visibility = View.VISIBLE

        val countdownImages = arrayOf(
            R.drawable.three,
            R.drawable.two,
            R.drawable.one
        )
        val handler = Handler(Looper.getMainLooper())
        var currentIndex = 0

        val countdownRunnable = object : Runnable{
            override fun run(){
                if (currentIndex < countdownImages.size){
                    //현재 인덱스에 맞는 이미지를 설정
                    countdownImageView.setImageResource(countdownImages[currentIndex])
                    countdownImageView.visibility = View.VISIBLE
                    currentIndex++

                    handler.postDelayed(this, 1000)
                }
                else{
                    //"시작" 이미지로 변경하고 카운트 다운 이미지 숨김
                    countdownImageView.visibility = View.GONE
                    startImageView.setImageResource(R.drawable.start)
                    startImageView.visibility = View.VISIBLE

                    //1초 후에 "시작" 이미지 숨김
                    handler.postDelayed({ startImageView.visibility = View.GONE }, 1000)
                }
            }
        }
        handler.post(countdownRunnable)
    }

    // 권한 요청 결과 처리
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

    // 권한 확인 메서드
    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    //백그라운드 스레드에서 초기화
    private fun initializeMediaPipe() {
        lifecycleScope.launch(Dispatchers.Default) {
            gestureRecognition = GestureRecognition(this@GameStartActivity)

            //손 인식을 위한 handLandmarkHelper
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
                        Log.d("HandActivity","time: $inferenceTime, resol: $width*$height")

                        val predictedIndices = mutableListOf<Int>()

                        for (result in resultBundle.results) {
                            if (result.landmarks().isNotEmpty()) {
                                for(idx in result.landmarks().indices){
                                    val predictedIndex = gestureRecognition.predictByResult(result, idx)
                                    // predictedIndex가 유효한 경우에만 로그 출력
                                    if (predictedIndex >= 0 && predictedIndex <= gestureLabels.size) {
                                        Log.d("HandActivity", "Predicted index: " + gestureLabels[predictedIndex])
                                        predictedIndices.add(predictedIndex)
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
                        if(predictedIndices.size!=0) {webSocketClient.sendMessage(message)}
                    }
                }
            )
            withContext(Dispatchers.Main) {
                startCamera()
            }
        }
    }
    private fun startCamera() {
    // 카메라 시작 메서드
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            //카메라 공급자를 가져옴
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
    
    //callBack으로 string을 받으면 UI 업데이트
    private fun handleNextProblem(problemInfo: String) {
        val problemNumbers = problemInfo.split(",").map { it.trim() }
        val gameImageLeftView = findViewById<ImageView>(R.id.gameImageLeftView)
        val gameImageRightView = findViewById<ImageView>(R.id.gameImageRightView)

        problemNumbers.forEachIndexed { index, numStr ->
            val num = numStr.toIntOrNull()
            if (num != null && num in handImages.indices) {
                if (index % 2 == 0) {
                    gameImageRightView.setImageResource(handImages[num])
                } else {
                    gameImageLeftView.setImageResource(handImages[num])
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // 소켓 연결 종료
        webSocketClient.disconnect()
    }
}