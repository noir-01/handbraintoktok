package com.example.myapplication.activity.game

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
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
import com.example.myapplication.BaseActivity
import com.example.myapplication.R
import com.example.myapplication.util.mediapipe.GestureRecognition
import com.example.myapplication.util.mediapipe.HandLandMarkHelper
import com.example.myapplication.util.ResourceUtils.imageResources
import com.example.myapplication.util.network.WebSocketClient
import com.example.myapplication.util.mediapipe.gestureLabels
import com.example.myapplication.util.network.RetrofitClient
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class GameStartActivity : BaseActivity(), WebSocketClient.WebSocketCallback {
    private lateinit var previewView: PreviewView
    private val CAMERA_REQUEST_CODE = 1001

    private lateinit var countdownImageView: ImageView
    private lateinit var startImageView: ImageView
    private lateinit var gameNextImageView: ImageView
    private lateinit var checkImageView: ImageView
    private lateinit var gameImageLeftView: ImageView
    private lateinit var gameImageCenterView: ImageView
    private  lateinit var gameImageRightView: ImageView

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var gestureRecognition: GestureRecognition
    private lateinit var handLandmarkerHelper: HandLandMarkHelper
    private lateinit var webSocketClient: WebSocketClient

    private var startTime = System.currentTimeMillis()
    private var lastMessage: String? = null
    private var isFirstProb = true
    private var probNum = AtomicInteger(0)
    private var countDownJob: Job?=null
    val tokenManager = RetrofitClient.getTokenManager()
    var socketInitMessage = tokenManager.getToken() + ","

    //websocket interface
    override fun onMessageReceived(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (message == lastMessage) {
                return@launch // 이전 메시지와 같으면 무시
            }
            lastMessage = message
            if (message.startsWith("next:")) {
                val problemInfo = message.substringAfter("next:")
                probNum.incrementAndGet()
                //정답이면(다음 문제 오면) 체크 표시 띄우고 잠시 멈췄다가 다음 문제 띄우기
                withContext(Dispatchers.Main) {
                    //첫번째 출제가 아닐때만 보이게
                    if(probNum.get()>1) {
                        checkImageView.setImageResource(R.drawable.checkmark)
                        checkImageView.bringToFront()
                        checkImageView.visibility = View.VISIBLE
                        delay(1500)
                        checkImageView.visibility = View.GONE
                    }
                    if (countDownJob?.isActive == true) {
                        Log.d("job","waiting")
                        countDownJob?.join()
                    }
                    handleNextProblem(problemInfo)
                }
                isFirstProb=false
                startTime = System.currentTimeMillis()
            }else if (message.startsWith("end,")) {
                val avgReactionTime = message.substringAfter("end,").toIntOrNull()

                withContext(Dispatchers.Main) {
                    checkImageView.setImageResource(R.drawable.checkmark)
                    checkImageView.bringToFront()
                    checkImageView.visibility = View.VISIBLE
                    delay(1500)
                    checkImageView.visibility = View.GONE

                    val intent = Intent(this@GameStartActivity,GameResultActivity::class.java)
                    intent.putExtra("REACTION",avgReactionTime?:0)
                    //결과 창 띄우고 게임 종료
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_start)
        
        //gameOptionActivity에서 putExtra로 전달
        val mode = intent.getStringExtra("MODE")
        //게임 모드를 initMessage에 붙여서 전송(COPY/RSP/CALC/RANDOM)
        socketInitMessage+=mode

        gameImageLeftView = findViewById<ImageView>(R.id.gameImageLeftView)
        gameImageCenterView = findViewById<ImageView>(R.id.gameImageCenterView)
        gameImageRightView = findViewById<ImageView>(R.id.gameImageRightView)
        countdownImageView = findViewById(R.id.countDownImageView)
//        startImageView = findViewById(R.id.startImageView)
        gameNextImageView = findViewById(R.id.gameNextImageView)
        checkImageView = findViewById(R.id.checkImageView)

        val backButton = findViewById<ImageButton>(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
        //countdownImageView.visibility = View.GONE
        //startImageView.visibility = View.GONE
        gameImageCenterView.visibility = View.GONE
        countdownImageView.visibility = View.GONE
        previewView = findViewById(R.id.camera_previewView)


        showPopupThenWebsocketConnect()

        // WebSocket
//        val serverDomain = getString(R.string.server_domain)
//        webSocketClient = WebSocketClient("wss://$serverDomain/ws", this)
//        CoroutineScope(Dispatchers.IO).launch{
//            webSocketClient.connect()
//            webSocketClient.sendMessage("8,true")
//        }

        if (allPermissionsGranted()) {
            initializeMediaPipe()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
            )
        }
    }
    private fun showPopupThenWebsocketConnect(){
        CoroutineScope(Dispatchers.Main).launch {
            showPopup {
                val serverDomain = getString(R.string.server_domain)
                webSocketClient = WebSocketClient("wss://$serverDomain/ws", this@GameStartActivity)

                // WebSocket 연결은 IO 스레드에서 실행
                CoroutineScope(Dispatchers.IO).launch {
                    delay(500)
                    webSocketClient.connect()  // WebSocket 연결
                    delay(500)
                    webSocketClient.sendMessage(socketInitMessage)  // 메시지 전송
                }
            }
        }
    }
    private fun showPopup(onComplete: () -> Unit) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Game Start")
            .setMessage("핸드폰을 흔들리지 않게 세워주세요.")
            .setPositiveButton("확인") { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    startCountdown()
                    onComplete() // 카운트다운 완료 후에 호출
                }
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
            R.drawable.ani_three,
            R.drawable.ani_two,
            R.drawable.ani_one,
        )

        var currentIndex = 0

        countDownJob = CoroutineScope(Dispatchers.Main).launch {
            while (currentIndex < countdownImages.size) {
                // 이미지 업데이트
                countdownImageView.setImageResource(countdownImages[currentIndex])
                countdownImageView.bringToFront()

                playCountdownSound(R.raw.countdown)

                currentIndex++
                delay(1000L) // 1초 대기
            }
            // 카운트다운 완료 처리
            countdownImageView.setImageResource(R.drawable.start)
            countdownImageView.visibility = View.VISIBLE
            playCountdownSound(R.raw.start_sound)

            // 1초 후 게임 이미지를 숨김
            delay(1000L)
            countdownImageView.visibility = View.GONE
        }
    }

    private fun playCountdownSound(res: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, res)
        mediaPlayer?.start()
        mediaPlayer?.setOnCompletionListener {
            it.release()
            mediaPlayer = null
        }
    }

    override fun finish() {
        CoroutineScope(Dispatchers.IO).launch {
            webSocketClient.disconnect()
            withContext(Dispatchers.Main){
                if(mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.stop()
                }
                super.finish()
            }
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

                        val predictedIndices = mutableListOf(-1,-1)

                        for (result in resultBundle.results) {
                            if (result.landmarks().isNotEmpty()) {
                                for (idx in result.landmarks().indices) {
                                    val handedness = result.handedness()[idx][0]
                                    val predictedIndex = gestureRecognition.predictByResult(result, idx)
                                    if (predictedIndex >= 0 && predictedIndex <= gestureLabels.size) {
                                        Log.d("HandActivity", "Predicted index: " + gestureLabels[predictedIndex])
                                        if (handedness.categoryName() == "Left") {
                                            predictedIndices[1] = predictedIndex
                                        } else if (handedness.categoryName() == "Right") {
                                            predictedIndices[0] = predictedIndex
                                        }
                                    }
                                }
                            } else {
                                Log.d("HandActivity", "No hand detected")
                            }
                        }
                        var message = predictedIndices.joinToString(",")

                        if (!(predictedIndices[0]==-1 && predictedIndices[1]==-1)) {
                            //reactionTime: 문제 출제했던 시간이 있음.
                            val reactionTime = System.currentTimeMillis() - startTime - inferenceTime
                            message = "$message,$reactionTime"
                            Log.d("reaction",message)
                            CoroutineScope(Dispatchers.IO).launch{
                                webSocketClient.sendMessage(message)
                            }
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

    private suspend fun handleNextProblem(problemInfo: String) {
        val problemNumbers = problemInfo.split(",").map { it.trim().toIntOrNull() }
        val gameType = problemNumbers.getOrNull(0)
        when(gameType) {
            // 따라하기
            0 -> {
                gameNextImageView.setImageResource(R.drawable.text_copy)
            }
            1->{
                gameNextImageView.setImageResource(R.drawable.text_win)
            }
            2->{
                gameNextImageView.setImageResource(R.drawable.text_lose)
            }
            3->{
                gameNextImageView.setImageResource(R.drawable.text_calc)
                val resourceId = imageResources["num_" + problemNumbers[1].toString()] ?:run {
                    // resourceId가 null일 경우 액티비티 종료
                    finish()
                    return@run // 추가적으로 return해서 이후 코드를 실행하지 않도록 합니다.
                }
                gameImageLeftView.visibility = View.GONE
                gameImageRightView.visibility = View.GONE
                gameImageCenterView.setImageResource(resourceId as Int)
                gameImageCenterView.visibility=View.VISIBLE
                gameImageCenterView.bringToFront()
            }

        }
        if(gameType!=3){
            val num1 = problemNumbers.getOrNull(1)
            val num2 = problemNumbers.getOrNull(2)
            Log.d("Nums","$num1, $num2")
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
                    gameImageLeftView.visibility = View.VISIBLE
                    Log.d("Nums","hand_" + gestureLabels[it] + "_l")
                } ?: run{
                    gameImageLeftView.visibility = View.GONE
                }
                num2?.let {
                    gameImageRightView.setImageResource(imageResources["hand_" + gestureLabels[it] + "_r"]!!)
                    gameImageRightView.visibility = View.VISIBLE
                    Log.d("Nums","hand_" + gestureLabels[it] + "_r")
                } ?: run{
                    gameImageRightView.visibility = View.GONE
                }
            }
        }
    }
}
