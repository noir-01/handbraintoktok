package com.example.myapplication.util.dataClass

import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// FloatRange에 random() 함수 추가
fun ClosedFloatingPointRange<Float>.random(): Float {
    return (start + Math.random() * (endInclusive - start)).toFloat()
}

object DummyData {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val today = LocalDate.now()

    // 더미 사용자 데이터
    val dummyUsers = listOf(
        UserDto(1, "User1", 1955),
        UserDto(2, "User2", 1963),
        UserDto(3, "User3", 1972),
        UserDto(4, "User4", 1981),
        UserDto(5, "User5", 1990)
    )

    // 더미 게임 기록 데이터
    val dummyGameRecords: Map<String, Map<String, List<RandomGameHistoryDto>>> = generateGameRecords()

    // 나이대별 게임 기록 생성
    private fun generateGameRecords(): Map<String, Map<String, List<RandomGameHistoryDto>>> {
        val ageGroups = dummyUsers.groupBy { user ->
            val birthDecade = (user.birthYear / 10) * 10
            "$birthDecade~${birthDecade + 9}년생"
        }
        Log.d("DummyData", "Age Groups: $ageGroups")

        val result = ageGroups.mapValues { (decade, groupUsers) ->
            Log.d("DummyData", "Processing Decade: $decade")
            val games = listOf("RANDOM", "RSP", "CALC", "COPY")
            games.associateWith { gameType ->
                groupUsers.flatMap { user ->
                    (0 until 14).map { daysAgo ->
                        RandomGameHistoryDto(
                            startDate = today.minusDays(daysAgo.toLong()).format(formatter),
                            averageReactionTime = (1.5f..2.5f).random() // Adjust range as needed
                        )
                    }
                }
            }
        }
        Log.d("DummyData", "Generated Game Records: $result")
        return result
    }

    // 나이대에 따른 반응 시간 생성
    private fun getReactionTimeByDecade(birthYear: Int, min: Float, max: Float): Float {
        return when {
            birthYear < 1960 -> (min..max).random()
            birthYear < 1970 -> (min - 0.1f..max - 0.1f).random()
            birthYear < 1980 -> (min - 0.2f..max - 0.2f).random()
            birthYear < 1990 -> (min - 0.3f..max - 0.3f).random()
            else -> (min - 0.4f..max - 0.4f).random()
        }
    }
}
