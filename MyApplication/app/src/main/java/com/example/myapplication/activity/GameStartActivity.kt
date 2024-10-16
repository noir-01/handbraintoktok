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

import androidx.lifecycle.lifecycleScope
import com.example.myapplication.util.GestureRecognition
import com.example.myapplication.util.HandLandMarkHelper
import com.google.mediapipe.tasks.vision.core.RunningMode

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

import com.example.myapplication.R
import com.example.myapplication.util.gestureLabels


class GameStartActivity : BaseActivity() {
    private lateinit var previewView: PreviewView
    private val CAMERA_REQUEST_CODE = 1001

    private lateinit var countdownImageView: ImageView
    private lateinit var startImageView: ImageView

    private lateinit var handLandmarkerHelper: HandLandMarkHelper
    private lateinit var gestureRecognition: GestureRecognition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_start)

        countdownImageView = findViewById(R.id.countdownImageView)
        startImageView = findViewById(R.id.startImageView)

        countdownImageView.visibility = View.GONE
        startImageView.visibility = View.GONE

        showPopup()

        // PreviewView 초기화
        previewView = findViewById(R.id.camera_previewView)

        // 전달받은 게임 이름 처리
        val gameName = intent.getStringExtra("GAME_NAME") ?: "Unknown Game"

        // 게임 이미지 처리
        val gameImageView = findViewById<ImageView>(R.id.gameImageRightView)
        val imageResource = when (gameName) {
            "mimic" -> R.drawable.mimic
            "rps" -> R.drawable.rps
            "bwf" -> R.drawable.bwf
            "random" -> R.drawable.random
            else -> R.drawable.default_image
        }
        gameImageView.setImageResource(imageResource)

        // 카메라 권한 체크 및 요청
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
            )
        }

        gestureRecognition = GestureRecognition(this)

        //손 인식을 위한 handLandmarkHelper
        handLandmarkerHelper = HandLandMarkHelper(
            context = this,
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
                    for (result in resultBundle.results) {
                        if (result.landmarks().isNotEmpty()) {
                            val predictedIndex = gestureRecognition.predictByResult(result)
                            // predictedIndex가 유효한 경우에만 로그 출력
                            if (predictedIndex >= 0 && predictedIndex <= gestureLabels.size) {
                                Log.d("HandActivity", "Predicted index: " + gestureLabels[predictedIndex])
                                predictedGesture.value = gestureLabels[predictedIndex] ?: "Unknown"
                            }
                        } else {
                            Log.d("HandActivity", "No hand detected")
                        }
                    }
                }
            }
        )
    }
    companion object {
        // 제스처 예측 결과를 상태로 관리하기 위한 변수
        var predictedGesture: MutableState<String> = mutableStateOf("No gesture detected")
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

    // 카메라 시작 메서드
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            //카메라 공급자를 가져옴
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
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
}