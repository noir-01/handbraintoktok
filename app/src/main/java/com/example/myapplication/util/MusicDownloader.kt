package com.example.myapplication.util

import android.content.Context
import com.example.myapplication.util.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MusicDownloader(private val apiService: ApiService, private val context: Context) {

    // 음악을 다운로드하여 특정 위치에 저장하는 함수
    suspend fun downloadMusicIfNotExists(musicId: Int): File? {
        val musicFile = File(context.getExternalFilesDir(null), "$musicId.mp3")

        if (musicFile.exists()) {
            return musicFile // 이미 다운로드된 경우 파일 반환
        }

        // 파일이 없으면 다운로드
        return withContext(Dispatchers.IO) {
            try {
                val responseBody = apiService.downloadMusic(musicId) // 다운로드 API 호출
                saveToFile(responseBody.byteStream(), musicFile)
                musicFile // 파일 저장 후 반환
            } catch (e: Exception) {
                e.printStackTrace()
                null // 다운로드 실패 시 null 반환
            }
        }
    }

    // InputStream을 파일에 저장하는 함수
    private fun saveToFile(inputStream: InputStream, file: File) {
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
    }
}