package com.example.handbraintoktok.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.handbraintoktok.util.GestureRecognition
import com.example.handbraintoktok.util.HandLandMarkHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private const val CAMERA_PERMISSION_CODE = 1001

val gestureLabels: Map<Int, String> = mapOf(
    0 to "middle_finger",
    1 to "heart",
    2 to "heart_twohands",
    3 to "thumb_up",
    4 to "v",
    5 to "ok",
    6 to "call",
    7 to "alien",
    8 to "baby",
    9 to "four",
    10 to "mandoo",
    11 to "one",
    12 to "rabbit",
    13 to "rock",
    14 to "three",
    15 to "two",
    16 to "eight",
    17 to "five",
    18 to "lucky_finger",
    19 to "seven",
    20 to "six",
    21 to "wolf"
)


class HandActivity : ComponentActivity() {

    private lateinit var handLandmarkerHelper: HandLandMarkHelper
    private lateinit var gestureRecognition: GestureRecognition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gestureRecognition = GestureRecognition(this)
        // Initialize the HandLandmarkerHelper
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
                    Log.d("HandActivity","time: $inferenceTime")
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

        // Check camera permission and request if necessary
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            setContent {
                GestureRecognitionScreen(handLandmarkerHelper)
            }
        }

    }
    companion object {
        // 제스처 예측 결과를 상태로 관리하기 위한 변수
        var predictedGesture: MutableState<String> = mutableStateOf("No gesture detected")
    }
}

@Composable
fun GestureRecognitionScreen(handLandmarkerHelper: HandLandMarkHelper) {
    // Column을 사용하여 화면을 두 부분으로 나눔
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 상단에 카메라 프리뷰
        CameraPreview(handLandmarkerHelper, Modifier.weight(.8f))

        // 하단에 제스처 예측 결과
        GestureResult(modifier = Modifier.weight(1f))
    }
}
@Composable
fun GestureResult(modifier: Modifier = Modifier) {
    // 제스처 결과를 표시할 Text 뷰
    val predictedGesture = HandActivity.predictedGesture.value

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Text(
            text = predictedGesture,
            fontSize = 24.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

// Composable function to show the camera preview
@Composable
fun CameraPreview(
    handLandmarkerHelper: HandLandMarkHelper,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    AndroidView({ previewView },modifier=modifier) { previewView ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Image analysis to feed frames to the HandLandmarker
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                handLandmarkerHelper.detectLiveStream(
                    imageProxy = imageProxy,
                    isFrontCamera = true
                )
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
