package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import androidx.camera.view.PreviewView
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.BaseActivity

import com.example.myapplication.R
import com.example.myapplication.adapters.MusicAdapter
import com.example.myapplication.util.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.myapplication.util.GestureRecognition
import com.example.myapplication.util.HandLandMarkHelper
import com.example.myapplication.util.Music
import com.example.myapplication.util.MusicRepository
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

        musicAdapter = MusicAdapter(musics = listOf()) { musicId ->
            selectedMusic = musicId // 선택된 음악의 ID 저장
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.adapter = musicAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

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

        loadMusicData()

    }

    private fun loadMusicData() {
        // Coroutine을 사용하여 비동기적으로 데이터를 가져옵니다
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val musicList = musicRepository.fetchMusics() // 데이터를 가져옴
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
}