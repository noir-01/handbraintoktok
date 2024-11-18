package com.example.myapplication.activity

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.camera.view.PreviewView
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.BaseActivity

import com.example.myapplication.R
import com.example.myapplication.adapters.MusicAdapter
import com.example.myapplication.adapters.RankingAdapter
import com.example.myapplication.util.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.myapplication.util.GestureRecognition
import com.example.myapplication.util.HandLandMarkHelper
import com.example.myapplication.util.Music
import com.example.myapplication.util.MusicRepository
import com.example.myapplication.util.Rank
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
        apiService = Retrofit.Builder()
            .baseUrl("https://$serverDomain")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        musicRepository = MusicRepository(apiService)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        musicAdapter = MusicAdapter(musics = listOf()) { music ->
            selectedMusic = music // 선택된 음악의 ID 저장,
            showRankingDialog(music.id)
        }


        recyclerView.adapter = musicAdapter

        //게임 시작 버튼
        findViewById<Button>(R.id.startRhythmGameButton).setOnClickListener {
            selectedMusic?.let { music ->
                // 선택된 음악 ID가 있으면, 게임 시작 화면으로 전달
                val intent = Intent(this, RhythmGameStartActivity::class.java)
                intent.putExtra("MUSIC_ID", music.id)
                intent.putExtra("DURATION", music.duration)
                //hard-coded, 나중에 난이도 선택 버튼 만들기.
                intent.putExtra("DIFFICULTY","HARD")
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "먼저 음악을 선택하세요!", Toast.LENGTH_SHORT).show()
            }
        }
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

    fun showRankingDialog(musicId:Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ranking, null)

        // 예시 데이터
        var rankingList = listOf(

            Rank(3, "User 3", 400),
            Rank(3, "User 3", 400),
            Rank(3, "User 3", 400),
            Rank(3, "User 3", 400),
            Rank(3, "User 3", 400),
            Rank(3, "User 3", 400),
            Rank(3, "User 3", 400),
        )

        // RecyclerView에 어댑터 설정
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rankingRecyclerView)
        val adapter = RankingAdapter(rankingList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 게임 시작 버튼 클릭 리스너
        val startButton = dialogView.findViewById<AppCompatButton>(R.id.startRhythmGameButton)
        startButton.setOnClickListener {
            Log.d("Start Game","Start")
        }

        // 다이얼로그 생성
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        //난이도 선택 버튼
        val easyButton = dialogView.findViewById<AppCompatButton>(R.id.easyButton)
        val normalButton = dialogView.findViewById<AppCompatButton>(R.id.normalButton)
        val hardButton = dialogView.findViewById<AppCompatButton>(R.id.hardButton)

        easyButton.setOnClickListener {
            rankingList = listOf(
                Rank(1, "Easy User 1", 300),
                Rank(2, "Easy User 2", 250),
                Rank(3, "Easy User 3", 200)
            )
            adapter.updateData(rankingList) // 어댑터 데이터 갱신
        }

        normalButton.setOnClickListener {
            rankingList = listOf(
                Rank(1, "Normal User 1", 400),
                Rank(2, "Normal User 2", 350),
                Rank(3, "Normal User 3", 300)
            )
            adapter.updateData(rankingList)
        }

        hardButton.setOnClickListener {
            rankingList = listOf(
                Rank(1, "Hard User 1", 500),
                Rank(2, "Hard User 2", 450),
                Rank(3, "Hard User 3", 400)
            )
            adapter.updateData(rankingList)
        }

        dialog.show()
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), (ActionBar.LayoutParams.WRAP_CONTENT))
    }
}