package com.example.myapplication.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.util.ApiService
import com.example.myapplication.util.MusicDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RhythmGameStartActivity: AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var musicDownloader: MusicDownloader
    private lateinit var gameBeats: List<Float>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_music_detail)

        //RhythmGameSelectActivity에서 전달받은 music_id
        val musicId = intent.getIntExtra("MUSIC_ID", -1)

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
                    Log.d("Music","downloaded!")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // musicId가 유효하지 않을 경우의 처리
            Toast.makeText(this, "음악 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun makeGameBeats(beats: List<Float>){
        
    }
}