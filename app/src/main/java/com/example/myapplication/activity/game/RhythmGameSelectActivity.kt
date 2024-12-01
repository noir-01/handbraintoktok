package com.example.myapplication.activity.game

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.camera.view.PreviewView
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.BaseActivity

import com.example.myapplication.R
import com.example.myapplication.adapters.MusicAdapter
import com.example.myapplication.adapters.RankingAdapter
import com.example.myapplication.util.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.myapplication.util.mediapipe.GestureRecognition
import com.example.myapplication.util.mediapipe.HandLandMarkHelper
import com.example.myapplication.util.dataClass.Music
import com.example.myapplication.util.MusicRepository
import com.example.myapplication.util.dataClass.Rank
import com.example.myapplication.util.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

class RhythmGameSelectActivity : BaseActivity(){
    private lateinit var previewView: PreviewView
    private val CAMERA_REQUEST_CODE = 1001

//    private lateinit var countdownImageView: ImageView
//    private lateinit var startImageView: ImageView

    private lateinit var handLandmarkerHelper: HandLandMarkHelper
    private lateinit var gestureRecognition: GestureRecognition

    private lateinit var apiService: ApiService
    private lateinit var musicRepository: MusicRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var musicAdapter: MusicAdapter
    private var selectedMusic: Music? = null
    /*
    * 1. 서버에 요청해서 곡 목록 받아와서 띄우기
    * 2. 곡 선택하면 그 곡 beat 받아오기
    * 3. 리듬게임 로직 적용
    * 4. 게임 플레이
    * */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhythmgame)

        val serverDomain = getString(R.string.server_domain)
//        apiService = Retrofit.Builder()
//        .baseUrl("https://$serverDomain")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//        .create(ApiService::class.java)
        apiService = RetrofitClient.apiService
        musicRepository = MusicRepository(apiService)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        musicAdapter = MusicAdapter(musics = listOf()) { music ->
            selectedMusic = music // 선택된 음악의 ID 저장,
            showRankingDialog(this, music.id)
        }


        recyclerView.adapter = musicAdapter

        //게임 시작 버튼
//        findViewById<Button>(R.id.startRhythmGameButton).setOnClickListener {
//            selectedMusic?.let { music ->
//                // 선택된 음악 ID가 있으면, 게임 시작 화면으로 전달
//                val intent = Intent(this, RhythmGameStartActivity::class.java)
//                intent.putExtra("MUSIC_ID", music.id)
//                intent.putExtra("DURATION", music.duration)
//                //hard-coded, 나중에 난이도 선택 버튼 만들기.
//                intent.putExtra("DIFFICULTY","HARD")
//                startActivity(intent)
//            } ?: run {
//                Toast.makeText(this, "먼저 음악을 선택하세요!", Toast.LENGTH_SHORT).show()
//            }
//        }
        Log.d("Music Load","loading..")
        loadMusicData()

    }

    private fun loadMusicData() {
        // Coroutine을 사용하여 비동기적으로 데이터를 가져옵니다
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val musicList = musicRepository.fetchMusics() // 데이터를 가져옴
                Log.d("MusicActivity", "Music Loaded")
                withContext(Dispatchers.Main){
                    musicAdapter.updateSongs(musicList) // 데이터를 어댑터에 전달
                    Log.d("MusicActivity", "Music List: $musicList")
                    musicAdapter.updateSongs(musicList)
                }
            } catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
    }

    fun showRankingDialog(context: Context, musicId:Int) {
        var selectedDifficulty: String? = null
        val dialogView = layoutInflater.inflate(R.layout.dialog_ranking, null)

        // 1. IO 스레드에서 순위 받아옴
        CoroutineScope(Dispatchers.IO).launch {
            val rhythmGameHistoryList = apiService.getRhythmRecords(musicId)
            // 2. Default 스레드에서 rankMap 만듦.
            withContext(Dispatchers.Default){
                val groupedByDifficulty = rhythmGameHistoryList.groupBy { it.difficulty }
                val rankMap = mutableMapOf<String, List<Rank>>()
                groupedByDifficulty.forEach { (difficulty, gameHistoryList) ->
                    // 점수 기준으로 내림차순 정렬
                    // 순위 매기며 Rank 객체 리스트 생성
                    val rankList = gameHistoryList.mapIndexed { index, rhythmGameHistoryDto ->
                        Rank(
                            ranking = index + 1, // 순위는 1부터 시작
                            name = rhythmGameHistoryDto.userDto.name,
                            combo = rhythmGameHistoryDto.combo,
                            score = rhythmGameHistoryDto.score
                        )
                    }
                    rankMap[difficulty] = rankList
                }
                withContext(Dispatchers.Main) {
                    // 예시: 초기 데이터를 다이얼로그에 보여주는 부분
                    var rankingList = rankMap["EASY"] ?: emptyList()

                    // RecyclerView에 어댑터 설정
                    val recyclerView =
                        dialogView.findViewById<RecyclerView>(R.id.rankingRecyclerView)
                    val adapter = RankingAdapter(rankingList)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = adapter
                    val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                    recyclerView.addItemDecoration(divider)
                    // 다이얼로그 생성
                    val dialog = AlertDialog.Builder(context)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create()

                    dialog.show()
                    dialog.window?.setLayout(
                        (resources.displayMetrics.widthPixels * 0.9).toInt(),
                        (ActionBar.LayoutParams.WRAP_CONTENT)
                    )

                    // 난이도 버튼
                    val easyButton = dialogView.findViewById<AppCompatButton>(R.id.easyButton)
                    val normalButton = dialogView.findViewById<AppCompatButton>(R.id.normalButton)
                    val hardButton = dialogView.findViewById<AppCompatButton>(R.id.hardButton)
                    val buttons = listOf(easyButton, normalButton, hardButton)
                    val startButton = dialogView.findViewById<AppCompatButton>(R.id.startRhythmGameButton)
                    startButton.setOnClickListener {
                        selectedMusic?.let { music ->
                            val intent = Intent(this@RhythmGameSelectActivity, RhythmGameStartActivity::class.java)
                            intent.putExtra("MUSIC_ID", music.id)
                            intent.putExtra("DURATION", music.duration)
                            //hard-coded, 나중에 난이도 선택 버튼 만들기.
                            intent.putExtra("DIFFICULTY", selectedDifficulty)
                            startActivity(intent)

                            ////결과창 테스트
//                            val totalScore = 12
//                            val combo = 0
//                            val intent = Intent(this@RhythmGameSelectActivity, RhythmGameResultActivity::class.java)
//                            intent.putExtra("MUSIC_ID", musicId)
//                            intent.putExtra("DIFFICULTY", selectedDifficulty)
//                            intent.putExtra("SCORE",totalScore)
//                            intent.putExtra("COMBO",combo)
//                            startActivity(intent)
//                            finish()

                            dialog.dismiss()
                            Log.d("Start Game", "Start")
                        }
                    }
                    fun updateButtonStyles(selectedButton: AppCompatButton?) {
                        buttons.forEach { button ->
                            if (button == selectedButton) {
                                button.setBackgroundColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.teal_700
                                    )
                                ) // 선택된 색상
                                button.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.white
                                    )
                                ) // 텍스트 색상
                            } else {
                                button.setBackgroundColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.teal_200
                                    )
                                ) // 기본 색상
                                button.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.teal_700
                                    )
                                ) // 기본 텍스트 색상
                            }
                        }
                    }

                    // 난이도 버튼 클릭 리스너
                    easyButton.setOnClickListener {
                        rankingList = rankMap["EASY"] ?: emptyList()
                        adapter.updateData(rankingList) // 어댑터 데이터 갱신
                        updateButtonStyles(easyButton)
                        selectedDifficulty = "EASY"
                    }

                    normalButton.setOnClickListener {
                        rankingList = rankMap["NORMAL"] ?: emptyList()
                        adapter.updateData(rankingList)
                        updateButtonStyles(normalButton)
                        selectedDifficulty = "NORMAL"
                    }

                    hardButton.setOnClickListener {
                        rankingList = rankMap["HARD"] ?: emptyList()
                        adapter.updateData(rankingList)
                        updateButtonStyles(hardButton)
                        selectedDifficulty = "HARD"
                    }
                }
            }
        }
    }
}