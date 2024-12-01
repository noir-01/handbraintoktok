package com.example.myapplication.activity.game

import androidx.appcompat.app.AppCompatActivity

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.LeaderboardAdapter
import com.example.myapplication.databinding.ActivityRhythmResultBinding // 변경된 부분
import com.example.myapplication.util.dataClass.RhythmGameHistoryDto
import com.example.myapplication.util.dataClass.RhythmGamePostDto
import com.example.myapplication.util.dataClass.UserScore
import com.example.myapplication.util.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RhythmGameResultActivity : AppCompatActivity() {
    private val apiService = RetrofitClient.apiService
    private var musicId = 1
    private lateinit var binding: ActivityRhythmResultBinding

    private var currentScore = 0 // 이번 게임 점수
    private var currentCombo=0
    private var previousRank = 0 // 이전 순위
    private var difficulty = ""
    private var newRank = 0 // 새로운 순위

    private var leaderboard = mutableListOf<UserScore>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //액티비티 생성시 extra로 전달하기
        musicId = intent.getIntExtra("MUSIC_ID",1)

        // View Binding 초기화
        binding = ActivityRhythmResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentScore=intent.getIntExtra("SCORE",0)

        //currentScore = 3

        currentCombo=intent.getIntExtra("COMBO",0)
        difficulty= intent.getStringExtra("DIFFICULTY").toString()


        CoroutineScope(Dispatchers.IO).launch {
            previousRank = getRankIndex()
            leaderboard = getRankRecordsBefore()
            apiService.uploadRhythmGameHistory(
                RhythmGamePostDto(musicId,difficulty,currentCombo,currentScore)
            )
            newRank = getRankIndex()
            val newLeaderBoard = getRankRecordsBefore()

            withContext(Dispatchers.Main){

                // 현재 점수 표시
                binding.scoreTextView.text = "🏆 이번 점수: $currentScore"
                binding.comboTextView.text = "🔥 최고 콤보: $currentCombo"

                val leaderboardSize= leaderboard.size
                setupLeaderboard(leaderboard)

                val leaderboardAdapter = binding.leaderboardRecyclerView.adapter as LeaderboardAdapter
                //animateRecyclerRank(previousRank,newRank)
                leaderboardAdapter.submitList(newLeaderBoard)
            }
        }
        binding.confirmButton.setOnClickListener {
//            val intent = Intent(this, RhythmGameSelectActivity::class.java) // 이동할 액티비티 설정
//            startActivity(intent)
            finish()
        }

    }


    private fun setupLeaderboard(leaderboard: MutableList<UserScore>) {
        binding.leaderboardRecyclerView.apply{
            adapter=LeaderboardAdapter(this@RhythmGameResultActivity)
            layoutManager=LinearLayoutManager(this@RhythmGameResultActivity)
            itemAnimator=DefaultItemAnimator().apply{
                moveDuration = 3000 // 애니메이션 지속 시간 조절
                addDuration = 3000
                removeDuration = 3000
            }
        }
        (binding.leaderboardRecyclerView.adapter as LeaderboardAdapter).submitList(leaderboard)
    }

    private suspend fun getRankRecordsBefore(): MutableList<UserScore> {
        return withContext(Dispatchers.IO) {
            val results: List<RhythmGameHistoryDto> = apiService.getRhythmRecords(musicId)
            val filteredResults = results.filter { it.difficulty == difficulty }
            filteredResults.mapIndexed { index, dto ->
                UserScore(
                    rank = index + 1, // 순위는 1부터 시작
                    name = dto.userDto.name,
                    score = dto.score,
                    combo = dto.combo,
                    id = dto.userDto.userId
                )
            }.toMutableList() // MutableList로 변환
        }
    }

    private suspend fun getRankIndex(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val result = apiService.getRhythmMyRank(musicId, difficulty = difficulty)
                if (result.isSuccessful) {
                    val ranking = result.body()?.get("ranking")?.toString()?.toDoubleOrNull()?.toInt()
                    return@withContext ranking ?: -1 // -1을 기본값으로 반환 (null인 경우)
                } else {
                    return@withContext -1 // 실패한 경우 기본값으로 -1 반환
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext -1 // 예외 발생 시 기본값으로 -1 반환
            }
        }
    }

}

