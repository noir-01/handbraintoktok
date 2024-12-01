package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.util.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddFriendActivity: AppCompatActivity() {
    //연락처 가져오기
    companion object {
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 200
    }
    private val REQUEST_CODE = 1

    val apiService = RetrofitClient.apiService
    private val tokenManager = RetrofitClient.getTokenManager()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isContactSyncDone = sharedPreferences.getBoolean("isContactSyncDone", false)
        //연락처 연동이 이미 된 상태면(거부도 완료로 간주) 바로 이동
        if(isContactSyncDone){
            moveToMain()
        }else{
            //안된 상태일때만
            setContentView(R.layout.activity_add_friend)

            findViewById<Button>(R.id.btn_no).setOnClickListener {
                //아니오 버튼: isContactSyncDone=true로 변경 후 메인으로 이동
                sharedPreferences.edit().putBoolean("isContactSyncDone", true).apply()
                moveToMain()
            }

            findViewById<Button>(R.id.btn_yes).setOnClickListener {

                //권한 없으면 권한 요청
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
                }else{
                    fetchAndMove(sharedPreferences)
                }
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었을 때
                val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                fetchAndMove(sharedPreferences)
            } else {
                Toast.makeText(this, "거부하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchAndMove(sharedPreferences: SharedPreferences){
        Toast.makeText(this, "연락처 연동 중...", Toast.LENGTH_SHORT).show()
        val contacts: List<String> = fetchContacts()
        CoroutineScope(Dispatchers.IO).launch{
            val response = apiService.uploadFriend(contacts)
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    val message = response.body() // "success" 또는 "Database error"

                    val serverMsg = message?.get("status")
                    if (message?.get("status") == "success") {
                        val friendNum = (message["friendNum"] as? Double)?.toInt() ?: 0
                        Toast.makeText(this@AddFriendActivity, "$friendNum" +"명 연동 성공", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@AddFriendActivity, "$serverMsg", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    // 예: "Database error" 처리
                    Toast.makeText(this@AddFriendActivity, "연동 실패: $errorMessage", Toast.LENGTH_LONG).show()
                }
                sharedPreferences.edit().putBoolean("isContactSyncDone", true).apply()
                moveToMain()
            }
        }
    }

    //연락처 가져오기
    private fun fetchContacts():List<String> {
        val contactNumbers = mutableSetOf<String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                if (numberIndex >= 0) {
                    var number = cursor.getString(numberIndex)
                    number = number.replace("-", "")
                    contactNumbers.add(number)
                }
            }

        }
        Log.d("TAG", contactNumbers.joinToString(", "))
        return contactNumbers.toList()
    }
    private fun moveToMain(){
        lifecycleScope.launch {
            delay(1000)
            val intent = Intent(this@AddFriendActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}