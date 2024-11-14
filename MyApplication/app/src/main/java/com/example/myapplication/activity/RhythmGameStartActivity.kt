package com.example.myapplication.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.graphics.Paint
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.util.ApiService
import com.example.myapplication.util.MusicDownloader
import com.example.myapplication.util.ResourceUtils.imageResources
import com.example.myapplication.util.durationToSec
import com.example.myapplication.util.gestureLabels
import com.example.myapplication.util.reversedGestureLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class RhythmGameStartActivity: AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var musicDownloader: MusicDownloader

    private val gameBeats =  mutableListOf<Float>()
    private val leftBeats =  mutableListOf<Float>()
    private val rightBeats =  mutableListOf<Float>()
    val leftImages = mutableListOf<String>()
    val rightImages = mutableListOf<String>()

    //이미지를 몇초 동안 보여줄건지
    private  var imageShowSec = 1.2f

    private lateinit var leftHandImageView: ImageView
    private lateinit var rightHandImageView: ImageView

    //음악 재생
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhythm_game_start)

        leftHandImageView = findViewById(R.id.leftHandImageView)
        rightHandImageView = findViewById(R.id.rightHandImageView)

        //RhythmGameSelectActivity에서 전달받은 music_id
        val musicId = intent.getIntExtra("MUSIC_ID", -1)
        val musicLength = durationToSec(intent.getStringExtra("DURATION")?:"00:00:00")

        val serverDomain = getString(R.string.server_domain)
        apiService = Retrofit.Builder()
            .baseUrl("https://$serverDomain")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
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
                        makeGameBeatsEasy(beats, bps)
                        playMusic(musicId) // 음악 재생을 메인 스레드에서 실행
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // musicId가 유효하지 않을 경우의 처리
            Toast.makeText(this, "음악 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    fun playMusic(musicId: Int) {
        val musicFile = File(getExternalFilesDir(null), "$musicId.mp3")
        mediaPlayer = MediaPlayer().apply {
            setDataSource(musicFile.absolutePath)
            prepareAsync() // 비동기 준비
            setOnPreparedListener {
                start() // 음악 시작
                trackMusicTime() // 음악 시간 추적 시작
            }
        }
    }

    fun trackMusicTime() {
        // Use lifecycleScope.launch to start a coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            while (mediaPlayer?.isPlaying == true) {
                val currentTime = getCurrentMusicTime()

                // Use withContext(Dispatchers.Main) to update the UI on the main thread
                withContext(Dispatchers.Main) {
                    // Update the images based on the current music time
                    updateImage(leftHandImageView, leftBeats, leftImages, currentTime)
                    updateImage(rightHandImageView, rightBeats, rightImages, currentTime)
                }
                // Add a small delay to avoid too frequent updates
                delay(16)  // Delay for 50ms (you can adjust this as needed)
            }
        }
    }
    fun getCurrentMusicTime(): Double {
        return mediaPlayer?.currentPosition?.toDouble()?.div(1000) ?: 0.0
    }
    fun updateImage(imageView: ImageView, beats: List<Float>, images: List<String>, currentTime: Double) {
        for (i in beats.indices) {
            if (currentTime >= beats[i] - imageShowSec && currentTime < beats[i]) {
                //showImageWithRectangle(imageView, images[i], currentTime, beats[i])  // 이미지 표시
                showImage(imageView,images[i],currentTime,beats[i])
            } else if (currentTime >= beats[i]) {
                hideImage(imageView)  // 이미지 숨기기
            }
        }
    }
    fun showImage(imageView: ImageView, imageName: String,currentTime:Double, beatTime:Float) {
        val resourceId = imageResources[imageName] ?: return // 리소스 ID가 없다면 return

        val originalBitmap = BitmapFactory.decodeResource(resources, resourceId)
        val originalHeight = originalBitmap.height
        val originalWidth = originalBitmap.width

        val imageViewHeight = imageView.height
        val imageViewWeight = imageView.width

        val bitmapWithBorder = Bitmap.createBitmap(originalWidth, originalHeight*2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapWithBorder)
        canvas.drawBitmap(originalBitmap, 0f, originalHeight*0.5f, null)  // 원본 이미지를 먼저 그립니다.

        val borderPaint = Paint()
        borderPaint.color = Color.BLACK
        borderPaint.strokeWidth = 50f
        borderPaint.style = Paint.Style.STROKE

        // 테두리 그리기 (이미지 크기에 맞춰 사각형 테두리 추가)
        canvas.drawRect(0f, originalHeight.toFloat()*0.5f, originalBitmap.width.toFloat(), originalHeight.toFloat()*1.5f, borderPaint)


        val borderPaint2 = Paint()
        borderPaint.color = Color.RED
        borderPaint.strokeWidth = 50f
        borderPaint.style = Paint.Style.STROKE
        // 바깥쪽 직사각형 그리기
        val remainingTime = beatTime - currentTime.toFloat()
        val rate = remainingTime / imageShowSec

        val left = 0f
        val right = originalWidth.toFloat()  // 너비는 그대로 유지
//        val top = originalHeight * 0.25f + originalHeight * 0.25f * (1-rate)
//        val bottom = originalHeight * 1.75f - originalHeight * 0.25f * (1-rate)
        val top = originalHeight * 0.5f * (1-rate)
        val bottom = originalHeight * 2f - originalHeight * 0.5f * (1-rate)


        canvas.drawRect(left, top, right, bottom, borderPaint)

        imageView.setImageBitmap(bitmapWithBorder)

    }


    fun hideImage(imageView: ImageView) {
        imageView.setImageDrawable(null)
    }
    fun showImageWithRectangle2(imageView: ImageView, imageName: String, currentTime: Double, beatTime: Float) {
        val resourceId = imageResources[imageName] ?: return // 리소스 ID가 없다면 return
        imageView.setImageResource(resourceId)
    }

    fun showImageWithRectangle(imageView: ImageView, imageName: String, currentTime: Double, beatTime: Float) {
        val resourceId = imageResources[imageName] ?: return // 리소스 ID가 없다면 return
        imageView.setImageResource(resourceId)

        // 이미지가 로드된 후 크기 얻기
        val drawable = imageView.drawable
        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()

        // 직사각형 그리기 (이미지 크기에 맞춰서 크기 조정)
        val rectSize = calculateRectangleSize(currentTime, beatTime, imageWidth, imageHeight)

        val borderDrawable = createRectangleDrawable(rectSize)
        val frameLayout = imageView.parent as? FrameLayout  // ImageView가 FrameLayout에 있어야 함

        // 새 직사각형을 추가하여 이미지 위에 그리기
        val rectView = View(imageView.context)
        rectView.background = borderDrawable
        val layoutParams = FrameLayout.LayoutParams(
            rectSize.toInt(), rectSize.toInt()
        )
        rectView.layoutParams = layoutParams

        // FrameLayout에 추가하여 직사각형 그리기
        frameLayout?.addView(rectView)

        // 직사각형의 위치를 ImageView의 위치에 맞추기
        layoutParams.leftMargin = (imageView.left + (imageView.width - rectSize).toInt()) / 2
        layoutParams.topMargin = (imageView.top + (imageView.height - rectSize).toInt()) / 2
        rectView.layoutParams = layoutParams
    }

    fun calculateRectangleSize(currentTime: Double, beatTime: Float, imageWidth: Float, imageHeight: Float): Float {
        val timeDiff = beatTime - currentTime.toFloat()
        val maxSize = Math.max(imageWidth, imageHeight) * 0.8f  // 최대 크기는 이미지 크기의 80%
        val minSize = Math.min(imageWidth, imageHeight) * 0.2f  // 최소 크기는 이미지 크기의 20%

        val shrinkFactor = timeDiff / (imageShowSec.toFloat())  // 시간에 비례하여 크기 변화

        return max(minSize, maxSize * shrinkFactor)  // 최소 크기 이하로는 줄어들지 않도록
    }

    fun createRectangleDrawable(size: Float): Drawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.TRANSPARENT)  // 직사각형 내부 색상
        shape.setStroke(10, Color.RED)  // 테두리 색상 및 두께
        shape.setSize(size.toInt(), size.toInt())  // 크기 설정
        return shape
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()  // 액티비티 종료 시 MediaPlayer 해제
    }

    fun gameStart(handImages: Pair<List<String>,List<String>>, gameBeats: Pair<List<Float>,List<Float>>){
        var (leftHandImages,rightHandImages) = handImages
        var (leftHandBeats, rightHandBeats) = gameBeats
    }
    //Easy: 번갈아서? 시간 넉넉하게, 어려운 손동작 빼기. [왼손/오른손 이미지 이름, 왼손/오른손 비트 시간 반환]
    fun makeGameBeatsEasy(beats: List<Float>, bps:Float): Pair<Pair<List<String>, List<String>>, Pair<List<Float>, List<Float>>>{
        val beatsSize = beats.size
        var i = 0

        //가운데, alien, seven, wolf
        val numbersToExclude = listOf(
            reversedGestureLabels["middle_finger"],
            reversedGestureLabels["heart"],
            reversedGestureLabels["seven"],
            reversedGestureLabels["eight"],
            reversedGestureLabels["wolf"],
            reversedGestureLabels["alien"],
            )
        val handIndexes = (0..21).toList() - numbersToExclude

        while(i<beatsSize){
            i += Random.nextInt(3)+5    //5,6,7초 중 랜덤으로
            if(i>=beatsSize) break
            gameBeats.add(beats[i])
        }
        //번갈아가면서 게임
        for ((index, value) in gameBeats.withIndex()) {
            if(index%2==0){
                leftBeats.add(value)
                gestureLabels[handIndexes.random()]?.let {
                    leftImages.add("hand_"+it+"_l")
                }
            }else{
                rightBeats.add(value)
                gestureLabels[handIndexes.random()]?.let {
                    rightImages.add("hand_"+it+"_r")
                }
            }
        }
        return Pair(Pair(leftImages,rightImages),Pair(leftBeats,rightBeats))
    }
    //Normal: 번갈아서. 시간 간격 조금 짧게
    //HARD:   손동작 동시에 출현, 시간 간격 더 짧게.
}