package com.example.myapplication.util.network

import com.example.myapplication.util.dataClass.Music
import com.example.myapplication.util.dataClass.NumDto
import com.example.myapplication.util.dataClass.RandomGameHistoryDto
import com.example.myapplication.util.dataClass.RhythmGameHistoryDto
import com.example.myapplication.util.dataClass.RhythmGamePostDto
import com.example.myapplication.util.dataClass.VerificationRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/music/getMusicList")
    suspend fun  getMusicList(): List<Music>

    @GET("/music/{musicId}/getBeatList")
    suspend fun getBeats(@Path("musicId") songId: Int): List<Float>

    @GET("/music/download/{musicId}")
    suspend fun downloadMusic(@Path("musicId") songId: Int): ResponseBody

    @GET("/history/rhythm/get/{musicId}")
    suspend fun getRhythmRecords(@Path("musicId") musicId: Int): List<RhythmGameHistoryDto>

    @GET("/history/random/get")
    suspend fun getRandomHistory(
        @Query("gameType") gameType:String,
        @Query("period") period:String
    ): List<RandomGameHistoryDto>

    @GET("/history/rhythm/get/{musicId}/{difficulty}")
    suspend fun getRhythmMyRank(@Path("musicId") musicId: Int, @Path("difficulty") difficulty:String): Response<Map<String,Any>>

    @GET("/get/myname")
    suspend fun getMyName(): Response<Map<String,Any>>

    @POST("/history/rhythm/upload")
    suspend fun uploadRhythmGameHistory(@Body rhythmGamePostDto: RhythmGamePostDto)

    @POST("/friend/upload")
    suspend fun uploadFriend(@Body contacts:List<String>): Response<Map<String, Any>>

    // Response<200>, {"state" : "success"}
    // Response<500>, {"state" : "token expired"}
    @POST("/login/token")
    suspend fun login(): Response<Unit>

    // Response<200>,{"message" : "success"}
    @POST("/sms/send")
    suspend fun sendSms(@Body numDto: NumDto): Response<Unit>
    
    //연동 해제
    @POST("/friend/unlink")
    suspend fun unlink(): Response<Map<String,Any>>
    
    //계정 탈퇴
    @POST("/user/deactivate")
    suspend fun deactivate(): Response<Map<String,Any>>

    /* 응답 꼴
    * Response<200>, {"token"  : "...."}
    * Response<401>, {"status" : "인증번호 틀림"}
    * Response<404>, {"status" : "유저 없음"}
    * Response<409>, {"status" : "이미 가입된 유저입니다"}
    * Response<500>, {"status" : "서버 오류"} <= 암호화 과정에서 오류
    */
    @POST("/sms/verify/register")
    suspend fun verifyCode(@Body verificationRequest: VerificationRequest): Response<Map<String, Any>>


    @POST("/sms/verify/refresh")
    suspend fun verifyRefresh(@Body verificationRequest: VerificationRequest): Response<Map<String, Any>>

}

fun durationToSec(duration: String): Int {
    val parts = duration.split(":")
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val seconds = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return hours * 3600 + minutes * 60 + seconds
}