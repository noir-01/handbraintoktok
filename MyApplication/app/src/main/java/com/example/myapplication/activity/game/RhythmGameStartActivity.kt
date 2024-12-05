package com.example.myapplication.activity.game

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Size
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
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
import com.example.myapplication.util.mediapipe.GestureRecognition
import com.example.myapplication.util.mediapipe.HandLandMarkHelper
import com.example.myapplication.util.MusicDownloader
import com.example.myapplication.util.ResourceUtils.imageResources
import com.example.myapplication.util.dataClass.RhythmGamePostDto
import com.example.myapplication.util.mediapipe.difficulty
import com.example.myapplication.util.network.durationToSec
import com.example.myapplication.util.mediapipe.gestureLabels
import com.example.myapplication.util.mediapipe.reversedGestureLabels
import com.example.myapplication.util.network.MyHttpServer
import com.example.myapplication.util.network.RetrofitClient
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.ceil
import kotlin.random.Random
import android.media.MediaMetadataRetriever
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.widget.ImageButton

class RhythmGameStartActivity: AppCompatActivity() {
    //mp3 보내줄 로컬 서버
    private lateinit var server :MyHttpServer
    
    private val apiService = RetrofitClient.apiService
    private lateinit var musicDownloader: MusicDownloader

    private val CAMERA_REQUEST_CODE = 1001
    private lateinit var previewView: PreviewView
    private lateinit var gestureRecognition: GestureRecognition
    private lateinit var handLandmarkerHelper: HandLandMarkHelper

    private val delayTime = 0.3f

    private val gameBeats =  mutableListOf<Float>()
    private val leftBeats =  mutableListOf<Float>()
    private val rightBeats =  mutableListOf<Float>()
    val leftImages = mutableListOf<String>()
    val rightImages = mutableListOf<String>()
    private val leftAnswers = mutableListOf<Int>()
    private val rightAnswers = mutableListOf<Int>()


    private var leftHandIndex: Int? = null
    private var rightHandIndex: Int? = null

    //이미지를 몇초 동안 보여줄건지
    private  var imageShowTime = 1.5f

    private lateinit var leftHandImageView: ImageView
    private lateinit var rightHandImageView: ImageView
    private lateinit var middleImageView: ImageView
    private lateinit var comboTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var webView:WebView
    private lateinit var leftXImageView: ImageView
    private lateinit var rightXImageView: ImageView
    private lateinit var addScoreView: TextView

    //콤보, 해당 회차 맞췄는지 확인하는 플래그
    private var combo = 0
    private var maxCombo = 0
    private var leftAnswerFlag = false
    private var rightAnswerFlag = false
    //총점 및 한번 맞췄을 때 점수
    //점수는 기본 점수*콤보 배열을 더해서 계산.
    private var totalScore = 0
    private val correctScore = listOf(100,150,200)
    private var difficultyInt = 0
    private var difficultyString = "EASY"

    //직접 musicFinishHandler 구현
    private var musicStartTime = 0.0
    private var duration = 0.0
    private val handler = Handler(Looper.getMainLooper())
    private var musicId = 0



    //음악 재생
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhythm_game_start)

        if (allPermissionsGranted()) {
            initializeMediaPipe()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
            )
        }
        previewView = findViewById(R.id.rhythmGameCameraView)

        leftHandImageView = findViewById(R.id.leftHandImageView)
        rightHandImageView = findViewById(R.id.rightHandImageView)
        middleImageView = findViewById(R.id.gameImageCenterView)
        comboTextView = findViewById(R.id.comboTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        addScoreView = findViewById<TextView>(R.id.addScoreView)

        leftXImageView = findViewById(R.id.leftXImageView)
        leftXImageView.visibility=View.GONE
        rightXImageView = findViewById(R.id.rightXImageView)
        rightXImageView.visibility=View.GONE


        //MP3 로드해주는 로컬 웹서버 시작
        CoroutineScope(Dispatchers.IO).launch {
            server = MyHttpServer(this@RhythmGameStartActivity, 8080)
            server.start()
        }

        webView = findViewById(R.id.webView)
        //webView.settings.allowContentAccess=true
        webView.settings.allowFileAccess=true
        webView.settings.allowContentAccess=true
        webView.settings.apply {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }
        webView.setInitialScale(70)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                Log.d("WebViewLog", message?.message() ?: "No message") // Log JavaScript console output
                return super.onConsoleMessage(message)
            }
        }

        //RhythmGameSelectActivity에서 전달받은 music_id
        musicId = intent.getIntExtra("MUSIC_ID", -1)
        val musicLength = durationToSec(intent.getStringExtra("DURATION")?:"00:00:00")
        difficultyString = intent.getStringExtra("DIFFICULTY")?:"EASY"
        difficultyInt = difficulty[difficultyString]?:0

        val serverDomain = getString(R.string.server_domain)
        musicDownloader = MusicDownloader(apiService, this)

        if (musicId != -1) {
            lifecycleScope.launch(Dispatchers.IO){
                try {
                    val musicFile = musicDownloader.downloadMusicIfNotExists(musicId)
                    val beats = apiService.getBeats(musicId)
                    //1초당 몇비트, 난이도 조절에 이용. (n초 = m비트 건너뛰어야 함.)
                    val bps = beats.size / musicLength.toFloat()
                    Log.d("Music","downloaded!")

                    withContext(Dispatchers.Main) {
                        delay(3000)
                        //난이도 따라서 다르게 맵 설정
                        when(difficultyString){
                            "EASY"->{
                                makeGameBeatsEasy(beats,bps)
                                imageShowTime=1.8f
                            }
                            "NORMAL"->{
                                makeGameBeatsNormal(beats,bps)
                                imageShowTime=1.8f
                            }
                            "HARD"->{
                                makeGameBeatsHard(beats,bps)
                                imageShowTime=1.5f
                            }
                            else->{
                                makeGameBeatsEasy(beats,bps)
                            }
                        }
                        //음악 재생 및 배경 파형 생성
                        playMusicHtml()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // musicId가 유효하지 않을 경우의 처리
            Toast.makeText(this, "음악 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
        }

        val backButton = findViewById<ImageButton>(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
    }
    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun playMusicHtml(){
        val musicFile = File(getExternalFilesDir(null), "$musicId.mp3")
        val retriever = MediaMetadataRetriever()

        retriever.setDataSource(musicFile.absolutePath)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        duration = durationString?.toLong()?.toDouble()?.div(1000) ?: 0.0
        Log.d("check","$duration")

        val musicFilePath = "http://localhost:8080/mp3/$musicId.mp3"
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // You can call evaluateJavascript here, after the page has loaded
                CoroutineScope(Dispatchers.Main).launch {
                    super.onPageFinished(view, url)
                    webView.evaluateJavascript("loadAudio('$musicFilePath');",null)
                    delay(1000)
                    webView.evaluateJavascript("playAudio();", null)
                    musicStartTime=System.currentTimeMillis()/1000.0
                    trackMusicTimeByHtml()
                    handler.postDelayed(checkRunnable, 300)
                }
            }
        }
        webView.loadUrl("file:///android_asset/index2.html")
    }

    private val checkRunnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis() / 1000.0 // 현재 시간 (초 단위)
            val elapsedTime = currentTime - musicStartTime // 음악 시작 이후 경과 시간 (초 단위)
            // 만약 경과 시간이 음악의 총 길이를 넘으면 종료된 것으로 판단
            if (elapsedTime >= duration) {
                Log.d("check","elapsed: $elapsedTime")
                Log.d("check","total: $duration")
                onMusicFinished() // 음악 종료 처리
            } else {
                // 계속해서 체크
                handler.postDelayed(this, 300)
            }
        }
    }
    private fun onMusicFinished() {
        // 음악이 끝났을 때 해야 할 작업을 여기에 추가
        CoroutineScope(Dispatchers.IO).launch {
            apiService.uploadRhythmGameHistory(
                RhythmGamePostDto(musicId, difficultyString,combo,totalScore)
            )
            withContext(Dispatchers.Main){
                val intent = Intent(this@RhythmGameStartActivity, RhythmGameResultActivity::class.java)
                intent.putExtra("MUSIC_ID", musicId)
                intent.putExtra("DIFFICULTY", difficultyString)
                intent.putExtra("SCORE",totalScore)
                intent.putExtra("COMBO",maxCombo)
                startActivity(intent)
                finish()
            }
        }
        handler.removeCallbacks(checkRunnable) // 주기적 체크 중지
    }

//    //기존 mp3 파일로 재생할 경우
//    fun playMusic(musicId: Int) {
//
//        val musicFile = File(getExternalFilesDir(null), "$musicId.mp3")
//        mediaPlayer = MediaPlayer().apply {
//            setDataSource(musicFile.absolutePath)
//            prepareAsync() // 비동기 준비
//            setOnPreparedListener {
//                start()
//                trackMusicTime()
//            }
//            setOnCompletionListener {
//                CoroutineScope(Dispatchers.IO).launch {
//                    apiService.uploadRhythmGameHistory(
//                        RhythmGamePostDto(musicId, difficultyString,combo,totalScore)
//                    )
//                    withContext(Dispatchers.Main){
//                        val intent = Intent(this@RhythmGameStartActivity, RhythmGameResultActivity::class.java)
//                        intent.putExtra("MUSIC_ID", musicId)
//                        intent.putExtra("DIFFICULTY", difficultyString)
//                        intent.putExtra("SCORE",totalScore)
//                        intent.putExtra("COMBO",combo)
//                        startActivity(intent)
//                        finish()
//                    }
//                }
//            }
//        }
//    }
//    //기존 mp3 파일로 재생할 경우 trackMusicTime
//    fun trackMusicTime() {
//        // Use lifecycleScope.launch to start a coroutine
//        lifecycleScope.launch(Dispatchers.IO) {
//            var leftBeatIdx = 0
//            var rightBeatIdx = 0
//
//            while (mediaPlayer?.isPlaying == true) {
//                val currentTime = getCurrentMusicTime()
//
//                // Main Thread에서 UI 수정
//                withContext(Dispatchers.Main) {
//                    //현재 시간이 비트 시간을 넘어갔으면 다음 비트로 이동.
//                    if(leftBeatIdx<leftBeats.size)
//                        updateImage(true, leftHandImageView, currentTime, leftBeatIdx, leftHandIndex)
//                    if(rightBeatIdx<rightBeats.size)
//                        updateImage(false, rightHandImageView, currentTime, rightBeatIdx, rightHandIndex)
//
//                    //적당한 시간 안에 맞췄으면(플래그=true) 콤보, 점수 올리고 플래그 초기화
//                    if(leftBeatIdx<leftBeats.size && currentTime>leftBeats[leftBeatIdx]+delayTime){
//                        leftBeatIdx++
//                        //콤보는 20까지만 증가
//                        if(answerFlag && combo<21) combo++
//                        else if (!answerFlag) combo = 0
//                        //점수는 기본 점수* 콤보 배열을 더해서 계산.
//                        totalScore += correctScore[difficultyInt]*combo
//                        answerFlag=false
//                    }
//                    //오른손
//                    if(rightBeatIdx<rightBeats.size && currentTime>rightBeats[rightBeatIdx]+delayTime){
//                        rightBeatIdx++
//                        if(answerFlag && combo<21) combo++
//                        else if (!answerFlag) combo = 0
//                        totalScore += correctScore[difficultyInt]*combo
//                        answerFlag=false
//                    }
//                    updateScoreAndCombo()
//                    delay(30)  // Delay
//                }
//            }
//        }
//    }
//    fun getCurrentMusicTime(): Double {
//        return mediaPlayer?.currentPosition?.toDouble()?.div(1000) ?: 0.0
//    }


    fun trackMusicTimeByHtml(){
        lifecycleScope.launch(Dispatchers.IO) {
            var leftBeatIdx = 0
            var rightBeatIdx = 0
            //현재 음악 위치한 시간
            var currentTime = System.currentTimeMillis()/1000.0 - musicStartTime

            while (currentTime<duration) {
                currentTime = System.currentTimeMillis()/1000.0 - musicStartTime
                // Main Thread에서 UI 수정
                withContext(Dispatchers.Main) {
                    //현재 시간이 비트 시간을 넘어갔으면 다음 비트로 이동.
                    if(leftBeatIdx<leftBeats.size)
                        updateImage(true, leftHandImageView, currentTime, leftBeatIdx, leftHandIndex)
                    if(rightBeatIdx<rightBeats.size)
                        updateImage(false, rightHandImageView, currentTime, rightBeatIdx, rightHandIndex)
                    var addScore=0
                    //적당한 시간 안에 맞췄으면(플래그=true) 콤보, 점수 올리고 플래그 초기화
                    if(leftBeatIdx<leftBeats.size && currentTime>leftBeats[leftBeatIdx]+delayTime){
                        leftBeatIdx++
                        //콤보는 20까지만 증가
                        if(leftAnswerFlag && combo<20) {
                            combo++
                            if(combo>maxCombo) maxCombo = combo
                        }
                        //틀렸을 경우 콤보 초기화 및 X 이미지 표시
                        else if (!leftAnswerFlag) {
                            combo = 0
                            showXImage(leftXImageView)
                        }
                        //점수는 기본 점수* 콤보 배열을 더해서 계산.
                        addScore = correctScore[difficultyInt]*combo
                        leftAnswerFlag=false
                    }
                    //오른손
                    if(rightBeatIdx<rightBeats.size && currentTime>rightBeats[rightBeatIdx]+delayTime){
                        rightBeatIdx++
                        if(rightAnswerFlag && combo<21) {
                            combo++
                            if(combo>maxCombo) maxCombo = combo
                        }
                        //틀렸을 경우 콤보 초기화 및 X 이미지 표시
                        else if (!rightAnswerFlag){
                            combo = 0
                            showXImage(rightXImageView)
                        }
                        addScore = correctScore[difficultyInt]*combo
                        rightAnswerFlag=false
                    }
                    updateScore(totalScore,addScore)
                    updateCombo()

                    delay(50)  // Delay
                }
            }
        }
    }

    //틀렸을 경우 전달받은 뷰에 x 표시 잠깐 표시
    fun showXImage(xImageView: ImageView){
        CoroutineScope(Dispatchers.Main).launch{
            xImageView.visibility= View.VISIBLE
            xImageView.bringToFront()
            Log.d("showXImage","X")
            delay(500)
            xImageView.visibility= View.GONE
        }
    }
    fun updateImage(isLeft:Boolean, imageView: ImageView, currentTime: Double, beatIdx:Int, predictIdx: Int?){
        when(isLeft){
            true->{
                if(leftImages[beatIdx]=="hand_heart_twohands_l"){
                    leftImages[beatIdx]="hand_heart_twohands"
                }
                if (currentTime >= leftBeats[beatIdx] - imageShowTime && currentTime < leftBeats[beatIdx]+ 0.2f) {
                    showImage(imageView,leftImages[beatIdx],currentTime,leftBeats[beatIdx],predictIdx==leftAnswers[beatIdx])

                    Log.d("Update Imgae",leftImages[beatIdx])
                }else{
                    hideImage(imageView)
                }
            }
            false->{
                if(rightImages[beatIdx]=="hand_heart_twohands_r"){
                    rightImages[beatIdx]="hand_heart_twohands"
                }
                else if (currentTime >= rightBeats[beatIdx] - imageShowTime && currentTime < rightBeats[beatIdx]+ 0.2f) {
                    showImage(imageView,rightImages[beatIdx],currentTime,rightBeats[beatIdx],predictIdx==rightAnswers[beatIdx])
                    middleImageView.visibility=View.GONE
                    Log.d("Update Imgae",rightImages[beatIdx])
                }
                else{
                    middleImageView.visibility=View.GONE
                    hideImage(imageView)
                }
            }
        }
    }

    fun showImage(imageView: ImageView, imageName: String, currentTime:Double, beatTime:Float,isAnswer: Boolean) {
        val resourceId = imageResources[imageName] ?: return // 리소스 ID가 없다면 return

        val originalBitmap = BitmapFactory.decodeResource(resources, resourceId)
        val originalHeight = originalBitmap.height
        val originalWidth = originalBitmap.width

        val bitmapWithBorder = Bitmap.createBitmap(originalWidth, originalHeight*2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapWithBorder)
        canvas.drawBitmap(originalBitmap, 0f, originalHeight*0.5f, null)  // 원본 이미지를 먼저 그립니다.

        val borderPaint = Paint()
        borderPaint.color = Color.WHITE
        borderPaint.strokeWidth = 100f
        borderPaint.style = Paint.Style.STROKE

        //정답일 경우 초록색으로 그림, 두께 조금 더 두껍게
        if(currentTime >= beatTime - 0.2f  && currentTime < beatTime+delayTime && isAnswer){
            borderPaint.color = Color.GREEN
            borderPaint.strokeWidth = 150f
            if(imageView == leftHandImageView){
                Log.d("is answer","left true")
                leftAnswerFlag=true
            }else if (imageView == rightHandImageView){
                rightAnswerFlag=true
                Log.d("is answer","right true")
            }
            Log.d("RhythmGame","answer")
        }
        // 테두리 그리기 (이미지 크기에 맞춰 사각형 테두리 추가)
        canvas.drawRect(0f, originalHeight.toFloat()*0.5f, originalBitmap.width.toFloat(), originalHeight.toFloat()*1.5f, borderPaint)
        
        // 바깥쪽 직사각형 그리기
        val remainingTime = beatTime - currentTime.toFloat()
        val rate = remainingTime / imageShowTime
        val left = 0f
        val right = originalWidth.toFloat()  // 너비는 그대로 유지
        var top = originalHeight * 0.5f
        var bottom = originalHeight * 1.5f

        // beatTime 이전에는 작아지는 직사각형 그리기
        if(currentTime<beatTime) {
//        val top = originalHeight * 0.25f + originalHeight * 0.25f * (1-rate)
//        val bottom = originalHeight * 1.75f - originalHeight * 0.25f * (1-rate)
            top = originalHeight * 0.5f * (1 - rate)
            bottom = originalHeight * 2f - originalHeight * 0.5f * (1 - rate)
            canvas.drawRect(left, top, right, bottom, borderPaint)
            
        }
        //beatTime 넘으면 사이즈 맞춰서 그리기
        else{
            canvas.drawRect(left, top, right, bottom, borderPaint)
        }

        imageView.setImageBitmap(bitmapWithBorder)
        imageView.bringToFront()
    }

    fun hideImage(imageView: ImageView) {
        imageView.setImageDrawable(null)
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()  // 액티비티 종료 시 MediaPlayer 해제
        server.stop()
        webView.evaluateJavascript("pauseAudio();", null)
    }

    private fun initializeMediaPipe() {
        lifecycleScope.launch(Dispatchers.Default) {
            gestureRecognition = GestureRecognition(this@RhythmGameStartActivity)
            handLandmarkerHelper = HandLandMarkHelper(
                context = this@RhythmGameStartActivity,
                runningMode = RunningMode.LIVE_STREAM,
                handLandmarkerHelperListener = object : HandLandMarkHelper.LandmarkerListener {
                    override fun onError(error: String, errorCode: Int) {
                        Log.e("HandActivity", "Hand Landmarker error: $error")
                    }

                    override fun onResults(resultBundle: HandLandMarkHelper.ResultBundle) {
                        val inferenceTime = resultBundle.inferenceTime
                        val height = resultBundle.inputImageHeight
                        val width = resultBundle.inputImageWidth
                        //Log.d("HandActivity", "time: $inferenceTime, resol: $width*$height")

                        val predictedIndices = mutableListOf<Int>()

                        for (result in resultBundle.results) {
                            if (result.landmarks().isNotEmpty()) {
                                for (idx in result.landmarks().indices) {
                                    val handedness = result.handedness()[idx][0]
                                    val predictedIndex = gestureRecognition.predictByResult(result, idx)
                                    if(predictedIndex==2){
                                        rightHandIndex=2
                                        leftHandIndex=2
                                    }
                                    else if (predictedIndex >= 0 && predictedIndex <= gestureLabels.size) {
                                        //Log.d("HandActivity", "Predicted index: " + gestureLabels[predictedIndex])
                                        if (handedness.categoryName() == "Left") {
                                            rightHandIndex = predictedIndex
                                        } else if (handedness.categoryName() == "Right") {
                                            leftHandIndex = predictedIndex
                                        }
                                        leftHandIndex?.let { predictedIndices.add(it) }
                                        rightHandIndex?.let { predictedIndices.add(it) }
                                    }
                                }
                            } else {
                                Log.d("HandActivity", "No hand detected")
                            }
                        }
                    }
                }
            )
            withContext(Dispatchers.Default){
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
    private fun updateScore(fromScore: Int, addScore: Int) {
        CoroutineScope(Dispatchers.Main).launch{
            scoreTextView.text = "점수: $totalScore"
            if(addScore!=0){
                addScoreView.text= "+$addScore"
                addScoreView.setTextColor(getColor(R.color.bright_yellow) )
                delay(500)
                addScoreView.setTextColor(getColor(R.color.black))
                totalScore+=addScore
                scoreTextView.text = "점수: $totalScore"
            }
        }
    }

    fun updateCombo(){
        if(combo!=0)
            comboTextView.text = "콤보: $combo     " // 콤보 텍스트 업데이트
    }
    //Easy: 번갈아서? 시간 넉넉하게, 어려운 손동작 빼기. [왼손/오른손 이미지 이름, 왼손/오른손 비트 시간 반환]
    fun makeGameBeatsEasy(beats: List<Float>, bps:Float): Pair<Pair<List<String>, List<String>>, Pair<List<Float>, List<Float>>>{
        val beatsSize = beats.size
        var i = 0

        //가운데, alien, seven, wolf
        val numbersToExclude = setOf(
            reversedGestureLabels["middle_finger"],
            reversedGestureLabels["heart"],
            reversedGestureLabels["seven"],
            reversedGestureLabels["eight"],
            reversedGestureLabels["wolf"],
            reversedGestureLabels["alien"],
            reversedGestureLabels["mandoo"],
            reversedGestureLabels["call"],

        )
        val handIndexes = (0..21).toList() - numbersToExclude

        while(i<beatsSize){
            i += Random.nextInt(3)+4    //4,5,6초 중 랜덤으로
            if(i>=beatsSize) break
            if(ceil(i*bps).toInt()<beatsSize)
                gameBeats.add(beats[ceil(i*bps).toInt()])
        }
        //번갈아가면서 게임
        for ((index, value) in gameBeats.withIndex()) {
            if(index%2==0){
                leftBeats.add(value)
                val nextGestureIdx = handIndexes.random()!!
                leftImages.add("hand_"+ gestureLabels[nextGestureIdx]+"_l")
                leftAnswers.add(nextGestureIdx)
            }else{
                rightBeats.add(value)
                val nextGestureIdx = handIndexes.random()!!
                rightImages.add("hand_"+ gestureLabels[nextGestureIdx]+"_r")
                rightAnswers.add(nextGestureIdx)
            }
        }
        return Pair(Pair(leftImages,rightImages),Pair(leftBeats,rightBeats))
    }
    //Normal: 번갈아서. 시간 간격 조금 짧게
    fun makeGameBeatsNormal(beats: List<Float>, bps:Float){
        val beatsSize = beats.size
        var i = 0

        //가운데, alien, seven, wolf
        val numbersToExclude = setOf(
            reversedGestureLabels["middle_finger"],
            reversedGestureLabels["mandoo"],
            reversedGestureLabels["wolf"],
            reversedGestureLabels["call"],
        )
        val handIndexes = (0..21).toList() - numbersToExclude
        while(i<beatsSize){
            i += Random.nextInt(3)+3    //3,4,5초 중 랜덤으로
            if(i>=beatsSize) break
            if(ceil(i*bps).toInt()<beatsSize)
                gameBeats.add(beats[ceil(i*bps).toInt()])
        }
        //번갈아가면서 게임
        for ((index, value) in gameBeats.withIndex()) {
            if(index%2==0){
                leftBeats.add(value)
                val nextGestureIdx = handIndexes.random()!!
                leftImages.add("hand_"+ gestureLabels[nextGestureIdx]+"_l")
                //leftImages.add("hand_heart_twohands_l")
                leftAnswers.add(nextGestureIdx)
            }else{
                rightBeats.add(value)
                val nextGestureIdx = handIndexes.random()!!
                rightImages.add("hand_"+ gestureLabels[nextGestureIdx]+"_r")
                //rightImages.add("hand_heart_twohands_r")
                rightAnswers.add(nextGestureIdx)
            }
        }
    }
    //HARD:   손동작 동시에 출현, 시간 간격 더 짧게.
    fun makeGameBeatsHard(beats: List<Float>, bps:Float){
        val beatsSize = beats.size
        var i = 0

        //가운데, alien, seven, wolf
        val numbersToExclude = setOf(
            reversedGestureLabels["middle_finger"],
            reversedGestureLabels["wolf"],
            reversedGestureLabels["call"],
        )
        var handIndexes = (0..21).toList() - numbersToExclude
        //일단 왼쪽에 몰아주기
        while(i<beatsSize){
            i += Random.nextInt(3)+3    //3,4,5초 중 랜덤으로
            if(i>=beatsSize) break
            //i초 = i초*bps 개의 beat
            if(ceil(i*bps).toInt()<beatsSize)
                leftBeats.add(beats[ceil(i*bps).toInt()])
        }

        var twohandsTimes = mutableListOf<Float>()
        for((idx,beatTime) in leftBeats.withIndex()){
            val nextGestureIdx = handIndexes.random()!!
            if(nextGestureIdx!=2)
                leftImages.add("hand_"+ gestureLabels[nextGestureIdx]+"_l")
            else {
                leftImages.add("hand_" + gestureLabels[nextGestureIdx])
                twohandsTimes.add(beatTime)
            }
            leftAnswers.add(nextGestureIdx)
        }
        //2번 제스쳐 나오면 앞뒤로 쉬었다가 다음거 뽑아야함.
        //오른손 비트 뽑고, 왼쪽에서 2번인 인덱스 확인하고 +- 3초인 구간 있으면 지우기.
        var j=0
        while(j<beatsSize){
            j += Random.nextInt(3)+3    //3,4,5초 중 랜덤으로
            if(j>=beatsSize) break
            rightBeats.add(beats[j])
        }
        var dropIndexes = mutableListOf<Int>()
        for(beatTime in twohandsTimes){
            for ((idx,beatTimeRight) in rightBeats.withIndex()){
                if (beatTimeRight - beatTime in -3f..3f) {
                    dropIndexes.add(idx)
                }
            }
        }
        Log.d("errorLog","$dropIndexes")
        dropIndexes.sortDescending()
        for(dropIdx in dropIndexes){
            rightBeats.removeAt(dropIdx)
        }
        //heart_twohands 빼고 뽑기
        handIndexes = handIndexes - listOf(2)
        for(beatTime in rightBeats){
            val nextGestureIdx = handIndexes.random()!!
            rightImages.add("hand_"+ gestureLabels[nextGestureIdx]+"_r")
            rightAnswers.add(nextGestureIdx)
        }
        Log.d("Hard Game","$rightImages.size, $rightAnswers.size")
    }
}