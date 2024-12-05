package com.example.myapplication.activity.tutorial

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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
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
import com.example.myapplication.R
import com.example.myapplication.activity.game.GameResultActivity
import com.example.myapplication.util.ResourceUtils.imageResources
import com.example.myapplication.util.mediapipe.GestureRecognition
import com.example.myapplication.util.mediapipe.HandLandMarkHelper
import com.example.myapplication.util.mediapipe.gestureLabels
import com.example.myapplication.util.network.WebSocketClient
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

//리듬게임 제외 게임들 설명: 각 2문제씩 출제?
class TutorialGameActivity: AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private val CAMERA_REQUEST_CODE = 1001

    private lateinit var mainView: View
    private lateinit var gameNextImageView: ImageView
    private lateinit var checkImageView: ImageView
    private lateinit var gameImageLeftView: ImageView
    private lateinit var gameImageCenterView: ImageView
    private  lateinit var gameImageRightView: ImageView

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var gestureRecognition: GestureRecognition
    private lateinit var handLandmarkerHelper: HandLandMarkHelper
    private var probNum = 0
    private var countDownJob: Job?=null
    private var startTime = System.currentTimeMillis()
    private lateinit var questions: MutableList<String>
    private lateinit var answers: MutableList<String>
    private lateinit var images: MutableList<Int>
    private var mode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_start)

        //gameOptionActivity에서 putExtra로 전달
        mode = intent.getStringExtra("MODE")?:""
        when(mode){
            "COPY"->{
                questions=mutableListOf("0,8,9","0,3,20")
                images= mutableListOf(
                    R.drawable.tutorial_copy_1,
                    R.drawable.tutorial_copy_2,
                )
            }
            "RSP"->{
                questions=mutableListOf("1,13,17","2,13,17")
                //바위 보 이김: 답은 보 가위
                //바위 보 짐: 답=바위 가위
                images= mutableListOf(
                    R.drawable.tutorial_rsp_win,
                    R.drawable.tutorial_rsp_lose,
                )
            }
            "CALC"->{
                questions=mutableListOf("3,9,0","3,6,0")
                images= mutableListOf(R.drawable.tutorial_calc)
            }
            "RANDOM"->{
                questions=mutableListOf("0,8,9","2,17,13")
                images= mutableListOf(R.drawable.tutorial_random)
            }
        }
        mainView=findViewById(R.id.myConstraintView)
        mainView.setBackgroundColor(getColor(R.color.black))

        val tutorialImageView = findViewById<ImageView>(R.id.tutorialImageView)
        tutorialImageView.bringToFront()
        val nextButton = findViewById<Button>(R.id.nextButton)
        val beforeButton = findViewById<Button>(R.id.beforeButton)
        var currentIndex = 0

        // 첫 번째 이미지 설정
        tutorialImageView.setImageResource(images[currentIndex])
        nextButton.visibility=View.VISIBLE
        nextButton.bringToFront()
        // 'Next' 버튼 클릭 이벤트
        nextButton.setOnClickListener {
            if (currentIndex < images.size - 1) {
                currentIndex++
                tutorialImageView.setImageResource(images[currentIndex])
            } else {
                // 마지막 이미지에서 버튼 비활성화 or 다른 작업
                nextButton.isEnabled = false
                nextButton.visibility=View.GONE
                beforeButton.visibility=View.GONE
                tutorialImageView.visibility=View.GONE

                showPopup{
                    mainView.setBackgroundResource(R.drawable.main_background)
                    handleNextProblem(questions[0])
                }
            }
        }
        beforeButton.visibility=View.VISIBLE
        beforeButton.bringToFront()
        // 'Next' 버튼 클릭 이벤트
        beforeButton.setOnClickListener {
            if (currentIndex > -1) {
                currentIndex--
                if(currentIndex>-1)
                    tutorialImageView.setImageResource(images[currentIndex])
            }
        }


        //게임 모드를 initMessage에 붙여서 전송(COPY/RSP/CALC/RANDOM)

        gameImageLeftView = findViewById<ImageView>(R.id.gameImageLeftView)
        gameImageCenterView = findViewById<ImageView>(R.id.gameImageCenterView)
        gameImageRightView = findViewById<ImageView>(R.id.gameImageRightView)

//        countdownImageView = findViewById(R.id.countdownImageView)
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
        previewView = findViewById(R.id.camera_previewView)


        if (allPermissionsGranted()) {
            initializeMediaPipe()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
            )
        }
    }

    private fun showPopup(onComplete: () -> Unit) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Game Start")
            .setMessage("핸드폰을 흔들리지 않게 세워주세요.")
            .setPositiveButton("확인") { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
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

    fun isAnswerAndCheckAndNext(answer1:Int, answer2: Int){
        CoroutineScope(Dispatchers.Main).launch {
            if(isAnswer(answer1,answer2)){
                probNum++
                checkImageView.setImageResource(R.drawable.checkmark)
                checkImageView.bringToFront()
                checkImageView.visibility = View.VISIBLE
                delay(1500)
                checkImageView.visibility = View.GONE
                if(probNum<2)
                    handleNextProblem(questions[probNum])
                else
                    finish()
            }
        }

    }
    fun isAnswer(answer1:Int, answer2: Int): Boolean{
        when(mode) {
            //questions = mutableListOf("0,8,9", "0,3,20")
            "COPY" -> {
                if(probNum==0){
                    return (answer1==8) && (answer2==9)
                }else{
                    return (answer1==3) && (answer2==20)
                }
            }
            "RSP" -> {
                //questions = mutableListOf("1,13,17", "2,13,17")
                //바위 보 이김: 답은 보 가위
                //바위 보 짐: 답=바위 가위
                if(probNum==0){
                    return (answer1==17) && ((answer2==4)||(answer2==15))
                }else{
                    return ((answer1==4)||(answer1==15)) && (answer2==13)
                }
            }

            "CALC" -> {
                //questions = mutableListOf("3,9,0", "3,4,0")
                if(probNum==0){
                    return defaultIntMapping(answer1) + defaultIntMapping(answer2) == 9
                }else{
                    return defaultIntMapping(answer1) + defaultIntMapping(answer2) == 6
                }
            }

            "RANDOM" -> {
                //questions = mutableListOf("0,8,9", "2,17,13")
                if(probNum==0){
                    return (answer1==8) && (answer2==9)
                }else{
                    return (answer1==13) && ((answer2==4)||(answer2==15))
                }

            }
        }
        return false
    }

    fun defaultIntMapping(input: Int): Int {
        return when (gestureLabels[input]) {
            "rock" -> 0
            "one", "thumb_up" -> 1
            "v", "two" -> 2
            "three", "six" -> 3
            "four" -> 4
            "five" -> 5
            else -> -1
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

    private fun isCorrect(): Boolean{
        return true
    }

    private fun initializeMediaPipe() {
        lifecycleScope.launch(Dispatchers.Default) {
            gestureRecognition = GestureRecognition(this@TutorialGameActivity)
            handLandmarkerHelper = HandLandMarkHelper(
                context = this@TutorialGameActivity,
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
                            isAnswerAndCheckAndNext(predictedIndices[0],predictedIndices[1])
                            Log.d("reaction",message)
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
                gameImageCenterView.visibility= View.VISIBLE
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