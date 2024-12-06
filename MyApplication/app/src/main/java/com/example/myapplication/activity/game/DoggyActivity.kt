package com.example.myapplication.activity.game

import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import android.app.Dialog
import android.content.Intent
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.File
import kotlin.random.Random

class DoggyActivity : AppCompatActivity() {

    private lateinit var dogImage: ImageView
    private lateinit var bgmPlayer: MediaPlayer
    private lateinit var dogEating: MediaPlayer
    private lateinit var toyUsing: MediaPlayer
    private lateinit var paper: MediaPlayer
    private lateinit var ziper: MediaPlayer

    // 상태 변수
    private var hunger: Int = 80
    private var happiness: Int = 90
    private var food: Int = 10
    private var ballCount: Int = 10
    private var boomerangCount: Int=3
    private var shampooCount : Int=2
    private var vitaminCount : Int =12
    private var waterCount : Int = 20

    // 슬롯 선택 여부
    private var isSlotSelected: Boolean = false

    // 핸들러 및 지연 시간
    private val handler = Handler(Looper.getMainLooper())
    private val hungerInterval = 5 * 60 * 1000L // 5분
    private val happinessInterval = 3 * 60 * 1000L // 3분

    private var lastAction: ActionType? = null

    // 데이터 파일 이름
    private val fileName = "pet_status.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doggy)

        dogImage = findViewById(R.id.dog)

        val cloudView1: ImageView = findViewById(R.id.cloud1)
        val cloudView2: ImageView = findViewById(R.id.cloud2)

        handler.post(object : Runnable{
            override fun run(){
                val time = Random.nextLong(6000, 12000)
                animateCloud(cloudView1, time)
                handler.postDelayed(this, time)
            }
        })
        handler.post(object : Runnable{
            override fun run(){
                val time = Random.nextLong(6000, 12000)
                animateCloud(cloudView2, time)
                handler.postDelayed(this, time)
            }
        })

        // BGM 초기화 및 시작
        initBGM()
        startActionLoop()

        dogEating = MediaPlayer.create(this, R.raw.dog_eating)
        toyUsing = MediaPlayer.create(this, R.raw.ruuber_duck_squeak)
        paper = MediaPlayer.create(this, R.raw.paper)
        ziper = MediaPlayer.create(this, R.raw.zipper)

        // "할일(미션)" 버튼 초기화
        val missionButton = findViewById<Button>(R.id.missionButton)

        // "가방" 버튼 클릭 초기화
        val inventoryButton = findViewById<Button>(R.id.inventoryButton)

        // "가방" 버튼 클릭 시 가방 팝업 열기
        inventoryButton.setOnClickListener {
            ziper.start()
            showInventoryDialog()
        }

        // "할일(미션)" 버튼 클릭 시 팝업 띄우기
        missionButton.setOnClickListener {
            paper.start()
            showMissionPopup()
        }

        // 상태 로드
        loadState()

        // UI 업데이트
        updateUI()

        // 배고픔 감소 루프 시작
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (hunger > 0) hunger -= 1
                saveState()
                updateUI()
                handler.postDelayed(this, hungerInterval)
            }
        }, hungerInterval)

        // 행복도 감소 루프 시작
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (happiness > 0) happiness -= 1
                saveState()
                updateUI()
                handler.postDelayed(this, happinessInterval)
            }
        }, happinessInterval)

        findViewById<Button>(R.id.feedButton).setOnClickListener {
            if (food > 0) {
                food -= 1
                happiness = (happiness + 10).coerceAtMost(100)
                hunger = (hunger + 10).coerceAtMost(100)
                saveState()
                updateUI()
                dogEating.start()
            }
        }
        findViewById<ImageView>(R.id.dog).setOnClickListener{
            // 강아지 클릭 동작
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            bgmPlayer.stop()
            saveState()
            finish()

        }

    }
    private fun initBGM() {
        // MediaPlayer 초기화
        bgmPlayer = MediaPlayer.create(this, R.raw.bgm)
        bgmPlayer.isLooping = true // 무한 반복 설정
        bgmPlayer.start() // 음악 재생
    }

    private fun showInventoryDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_inventory)

        // 인벤토리 슬롯 설정
        val gridLayout = dialog.findViewById<GridLayout>(R.id.inventoryGrid)
        gridLayout.removeAllViews()

        var selectedSlot: View? = null // 선택된 슬롯
        var selectedItem: String? = null // 선택된 아이템 ("ball" 또는 "boomerang")

        fun addItemListener(view: View, string: String){
            view.setOnClickListener {
                selectedSlot = view
                selectedItem = string
                highlightSelectedSlot(gridLayout, view)
            }
            gridLayout.addView(view)
        }

        // 공 슬롯 추가
        val ballSlotView = layoutInflater.inflate(R.layout.activity_slot_with_ball, gridLayout, false)
        val ballImage = ballSlotView.findViewById<ImageView>(R.id.ballImage)
        val ballCountText = ballSlotView.findViewById<TextView>(R.id.ballCountText)
        ballImage.setImageResource(R.drawable.ball)
        ballCountText.text = ballCount.toString()
        //공 클릭 리스너
        addItemListener(ballSlotView, "ball")


        // 부메랑 슬롯 추가
        val boomerangSlotView = layoutInflater.inflate(R.layout.activity_slot_with_boomerang, gridLayout, false)
        val boomerangImage = boomerangSlotView.findViewById<ImageView>(R.id.boomerangImage)
        val boomerangCountText = boomerangSlotView.findViewById<TextView>(R.id.boomerangCountText)
        boomerangImage.setImageResource(R.drawable.boomerang) // 부메랑 이미지  설정
        boomerangCountText.text = boomerangCount.toString() // 부메랑 개수 표시
        //부메랑 클릭 리스너
        addItemListener(boomerangSlotView, "boomerang")


        // 샴푸 슬롯 추가
        val shampooSlotView = layoutInflater.inflate(R.layout.activity_slot_with_shampoo, gridLayout, false)
        val shampooImage = shampooSlotView.findViewById<ImageView>(R.id.shampooImage)
        val shampooCountText = shampooSlotView.findViewById<TextView>(R.id.shampooCountText)
        shampooImage.setImageResource(R.drawable.shampoo) // 샴푸 이미지 설정
        shampooCountText.text = shampooCount.toString()// 샴푸 개수 표시
        //샴푸 클릭 리스너
        addItemListener(shampooSlotView, "shampoo")


        // 비타민 슬롯 추가
        val vitaminSlotView = layoutInflater.inflate(R.layout.activity_slot_with_vitamin, gridLayout, false)
        val vitaminImage = vitaminSlotView.findViewById<ImageView>(R.id.vitaminImage)
        val vitaminCountText = vitaminSlotView.findViewById<TextView>(R.id.vitaminCountText)
        vitaminImage.setImageResource(R.drawable.vitamin) // 비타민 이미지 설정
        vitaminCountText.text = vitaminCount.toString()// 비타민 개수 표시
        addItemListener(vitaminSlotView, "vitamin")

        // 물 슬롯 추가
        val waterSlotView = layoutInflater.inflate(R.layout.activity_slot_with_water, gridLayout, false)
        val waterImage = waterSlotView.findViewById<ImageView>(R.id.waterImage)
        val waterCountText = waterSlotView.findViewById<TextView>(R.id.waterCountText)
        waterImage.setImageResource(R.drawable.water) // 물 이미지 설정
        waterCountText.text = waterCount.toString()// 물 개수 표시
        addItemListener(waterSlotView, "water")


        // "사용하기" 버튼 동작 설정
        dialog.findViewById<Button>(R.id.useButton).setOnClickListener {
            when (selectedItem) {
                "ball" -> {
                    if (ballCount > 0) {
                        ballCount -= 1
                        happiness = (happiness + 3).coerceAtMost(100)
                        ballCountText.text = ballCount.toString()
                        showMessage("공을 사용했습니다!")
                    } else {
                        showMessage("공이 부족합니다!")
                    }
                }
                "boomerang" -> {
                    if (boomerangCount > 0) {
                        boomerangCount -= 1
                        happiness = (happiness + 5).coerceAtMost(100)
                        boomerangCountText.text = boomerangCount.toString()
                        showMessage("부메랑을 사용했습니다!")
                    } else {
                        showMessage("부메랑이 부족합니다!")
                    }
                }
                "shampoo" -> {
                    if (shampooCount > 0) {
                        shampooCount -= 1
                        happiness = (happiness + 3).coerceAtMost(100)
                        shampooCountText.text = shampooCount.toString()
                        showMessage("샴푸를 사용했습니다!")
                    } else {
                        showMessage("샴푸가 부족합니다!")
                    }
                }
                "vitamin" -> {
                    if (vitaminCount>0){
                        vitaminCount -= 1
                        happiness=(happiness + 3).coerceAtMost(100)
                        vitaminCountText.text=vitaminCount.toString()
                        showMessage("비타민을 사용했습니다!")
                    } else {
                        showMessage("비타민이 부족합니다!")
                    }
                }
                "water" -> {
                    if (waterCount > 0) {
                        waterCount -= 1
                        happiness = (happiness + 1).coerceAtMost(100)
                        waterCountText.text = waterCount.toString()
                        showMessage("물을 사용했습니다!")
                    } else {
                        showMessage("물이 부족합니다!")
                    }
                }
                else -> {
                    Toast.makeText(this, "사용할 아이템을 선택하세요!", Toast.LENGTH_SHORT).show()
                }
            }
            toyUsing.start()
            dialog.dismiss()
            updateUI()
        }



        // 닫기 버튼 설정
        dialog.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dialog.dismiss() }

        dialog.show()
    }

    private fun animateCloud(cloudView: ImageView, time: Long) {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // 구름을 랜덤하게 선택
        val cloudImages = listOf(R.drawable.cloud1, R.drawable.cloud2, R.drawable.cloud3, R.drawable.cloud4)
        cloudView.setImageResource(cloudImages.random())

        // 구름 초기 위치와 크기 설정
        cloudView.visibility = View.VISIBLE
        val randomY = Random.nextInt(0, 200) // Y축에서 랜덤 시작 위치
        val startX = -100 // 화면 왼쪽 바깥
        val endX = screenWidth + 100 // 화면 오른쪽 바깥
        cloudView.translationX = startX.toFloat()
        cloudView.translationY = randomY.toFloat()
        cloudView.scaleY = cloudView.scaleX


        ObjectAnimator.ofFloat(cloudView, "translationX", startX.toFloat(), endX.toFloat()).apply {
            duration = time // 총 time초간 이동
            start()
        }
    }


    private fun highlightSelectedSlot(gridLayout: GridLayout, selectedSlot: View) {
        // 모든 슬롯의 배경 초기화
        for (i in 0 until gridLayout.childCount) {
            gridLayout.getChildAt(i).setBackgroundResource(0)
        }
        // 선택된 슬롯 강조
        selectedSlot.setBackgroundResource(R.drawable.slot_selected_red)
    }

    private fun showMessage(message: String) {
        val messageView = findViewById<TextView>(R.id.itemMessage)
        messageView.text = message
        messageView.visibility = View.VISIBLE

        // 1초 후 메시지 숨기기
        Handler(Looper.getMainLooper()).postDelayed({
            messageView.visibility = View.GONE
        }, 3000)
    }

//    private fun toggleSlotSelection(slotView: View) {
//        if (isSlotSelected) {
//            slotView.setBackgroundResource(0) // 선택 해제
//            isSlotSelected = false
//        } else {
//            slotView.setBackgroundResource(R.drawable.slot_selected_red) // 선택된 슬롯 강조
//            isSlotSelected = true
//        }
//    }

    private fun showMissionPopup() {
        // 팝업 생성
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_popup_mission)

        // 팝업 내 버튼 초기화
        val btnHandGesture = dialog.findViewById<Button>(R.id.btnHandGesture)
        val btnRhythmGame = dialog.findViewById<Button>(R.id.btnRhythmGame)
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)

        // 버튼 클릭 이벤트 설정
        btnHandGesture.setOnClickListener {
            //음악 끄기
            bgmPlayer.stop()
            // 손동작 따라하기 동작
            val intent = Intent(this,GameStartActivity::class.java)
            intent.putExtra("MODE", "COPY")
            startActivity(intent)
            food = (food + 3).coerceAtMost(10)
            saveState()
            updateUI()
            dialog.dismiss()
        }

        btnRhythmGame.setOnClickListener {
            bgmPlayer.stop()
            // 리듬게임 1회 실행
        }

        btnClose.setOnClickListener {
            // 닫기 버튼 클릭 시 팝업 닫기
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateUI() {
        findViewById<TextView>(R.id.hungerText).text = "배고픔: $hunger/100"
        findViewById<TextView>(R.id.happinessText).text = "행복도: $happiness/100"
        findViewById<TextView>(R.id.foodText).text = "먹이 개수: $food/10"
    }

    private fun saveState() {
        val jsonObject = JSONObject().apply {
            put("hunger", hunger)
            put("happiness", happiness)
            put("food", food)
            put("lastSavedTime", System.currentTimeMillis())
            put("ballCount", ballCount)
            put("boomerangCount", boomerangCount)
            put("shampooCount", shampooCount)
            put("vitaminCount", vitaminCount)
            put("waterCount", waterCount)
        }

        try {
            val file = File(getExternalFilesDir(null), fileName)
            file.writeText(jsonObject.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadState() {
        try {
            val file = File(getExternalFilesDir(null), fileName)
            if (file.exists()) {
                val jsonData = file.readText()
                val jsonObject = JSONObject(jsonData)

                hunger = jsonObject.getInt("hunger")
                happiness = jsonObject.getInt("happiness")
                food = jsonObject.getInt("food")
                ballCount = jsonObject.getInt("ballCount")
                boomerangCount = jsonObject.getInt("boomerangCount")
                shampooCount = jsonObject.getInt("shampooCount")
                vitaminCount = jsonObject.getInt("vitaminCount")
                waterCount = jsonObject.getInt("waterCount")


                val lastSavedTime = jsonObject.getLong("lastSavedTime")
                val elapsedTime = System.currentTimeMillis() - lastSavedTime

                // 경과된 시간에 따라 배고픔 및 행복도 감소
                hunger -= (elapsedTime / hungerInterval).toInt()
                happiness -= (elapsedTime / happinessInterval).toInt()

                if (hunger < 0) hunger = 0
                if (happiness < 0) happiness = 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startActionLoop(){
        handler.post(object : Runnable{
            override fun run(){
                val randomAction = getRandomAction()
                performAction(randomAction)
                val nextDelay = randomAction.delay
                handler.postDelayed(this, nextDelay)
            }
        })
    }

    private fun getRandomAction(): ActionType {
        val weightedActions = mutableListOf<ActionType>()

        // IDLE1과 IDLE2의 확률을 높이기 위해 반복 횟수 증가
        repeat(5) { weightedActions.add(ActionType.IDLE1) } // IDLE1 빈도 높이기
        repeat(5) { weightedActions.add(ActionType.IDLE2) } // IDLE2 빈도 높이기

        // 나머지 액션은 기본 빈도로 추가
        weightedActions.add(ActionType.WALK)
        weightedActions.add(ActionType.RUN)
        weightedActions.add(ActionType.JUMP)
        weightedActions.add(ActionType.SIT)
        weightedActions.add(ActionType.SNIFF)
        weightedActions.add(ActionType.SNIFF_AND_WALK)
        if(hunger < 30){
            val temp = Random.nextInt(2);
            if(temp == 0) {
                return ActionType.IDLE1;
            } else{
                return ActionType.IDLE2;
            }
        };
        if(happiness < 30){
            dogImage.visibility = View.INVISIBLE;
        } else{
            dogImage.visibility = View.VISIBLE;
        }

        // 리스트에서 랜덤으로 ActionType 선택
        return weightedActions.random()
    }



    private fun performAction(actionType: ActionType) {
        dogImage.animate().cancel()
        when (actionType) {
            ActionType.WALK -> {
                AnimationUtils.startAnimation(dogImage, ActionType.WALK)
                moveDog(actionType.delay)
            }
            ActionType.RUN -> {
                AnimationUtils.startAnimation(dogImage, ActionType.RUN)
                moveDog(actionType.delay)
            }
            ActionType.IDLE1 -> {
                AnimationUtils.startAnimation(dogImage, ActionType.IDLE1)
            }
            ActionType.IDLE2 -> {
                AnimationUtils.startAnimation(dogImage, ActionType.IDLE2)
            }
            ActionType.SNIFF -> {
                randomItem()
                AnimationUtils.startAnimation(dogImage, ActionType.SNIFF)

            }
            ActionType.SNIFF_AND_WALK -> {
                randomItem()
                AnimationUtils.startAnimation(dogImage, ActionType.SNIFF_AND_WALK)
                moveDog(actionType.delay)
            }
            ActionType.SIT -> {
                AnimationUtils.startAnimation(dogImage, ActionType.SIT)

                // SIT 애니메이션의 지속 시간은 애니메이션 자체에 정의됨
                val animationDuration = Random.nextLong(2000L, 10000L)  // 2초에서 10초 사이의 랜덤 시간

                dogImage.postDelayed({
                    // SIT 애니메이션이 종료된 후, 랜덤한 시간동안 '가만히' 있는 상태로 설정
                    val idleDuration = Random.nextLong(2000L, 5000L)  // 2초에서 5초 사이의 랜덤 시간
                    handler.postDelayed({
                        // 가만히 있는 동안, IDLE 상태로 전환
                        val idleAction = if (Random.nextBoolean()) ActionType.IDLE1 else ActionType.IDLE2
                        AnimationUtils.startAnimation(dogImage, idleAction)
                    }, idleDuration)
                }, animationDuration)
            }

            ActionType.JUMP -> {
                AnimationUtils.startAnimation(dogImage, ActionType.JUMP)
                dogImage.animate()
                    .translationYBy(-100f)
                    .setDuration(500L)
                    .withEndAction {
                        dogImage.animate().translationYBy(100f).setDuration(500).start()
                    }
            }
        }
        // 마지막 행동 저장
        lastAction = actionType
    }

    private fun randomItem(){
        val randomChance =Random.nextInt(1, 101)
        if(randomChance >= 5){
            return;
        }
        val rand = Random.nextInt(0,5)
        var item: String?= null
        when(rand){
            0 ->{
                item = "공"
                ballCount += 1

            }
            1 ->{
                item = "부메랑"
                boomerangCount += 1
            }
            2 ->{
                item = "샴푸"
                shampooCount += 1
            }
            3 ->{
                item = "비타민"
                vitaminCount += 1
            }
            4 ->{
                item = "물"
                waterCount += 1
            }

        }
        saveState()
        showMessage("${item}을/를 찾았습니다!")
    }


    private fun moveDog(duration: Long) {
        // 강아지 이미지 크기
        val dogWidth = dogImage.width
        val dogHeight = dogImage.height

        // 핸드폰 화면 크기 가져오기
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // 화면 중심 기준으로 이동 가능한 범위 계산
        val centerX = 0f // 화면 중심이 0
        val centerY = 0f
        val maxX = (screenWidth / 2 - dogWidth / 2).toFloat() // 화면 오른쪽 끝
        val minX = -(screenWidth / 2 - dogWidth / 2).toFloat() // 화면 왼쪽 끝
        val maxY = (screenHeight / 2 - dogHeight / 2 - 100).toFloat() // 화면 아래쪽 끝
        val minY = -(screenHeight / 2 - dogHeight / 2 - 150).toFloat() // 화면 위쪽 끝

        // 현재 위치 가져오기
        val currentX = dogImage.translationX
        val currentY = dogImage.translationY

        // 랜덤 위치 계산
        var randomX = Random.nextFloat() * (maxX - minX) + minX
        var randomY = (Random.nextFloat() * (maxY - minY) + minY).coerceAtMost(350f)
        if(randomX > 350f && randomY < -700f){
            randomX -= 100f;
            randomY += 150f;
        }

        // 방향에 따라 이미지 즉시 뒤집기
        if (randomX < currentX) {
            dogImage.scaleX = -1f // 왼쪽으로 이동
        } else {
            dogImage.scaleX = 1f // 오른쪽으로 이동
        }

        // 화면 위쪽으로 갈수록 크기 감소
        val normalizedY = (randomY - minY) / (maxY - minY) // Y 범위를 [0, 1]로 정규화
        val scale = 0.5f + 0.5f * normalizedY // 최소 0.5, 최대 1.0 크기로 비율 조정

        // 크기 조정 및 이동 애니메이션
        dogImage.animate()
            .translationX(randomX)
            .translationY(randomY)
            .scaleX(dogImage.scaleX) // 현재 방향 유지
            .scaleY(scale) // 크기 변화
            .setDuration(duration)
            .start()
    }

    private fun stopMoveMent(){
        dogImage.animate().cancel()
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

}
