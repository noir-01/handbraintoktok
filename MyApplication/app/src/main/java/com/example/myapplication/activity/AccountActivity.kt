package com.example.myapplication.activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.RegisterOrNotActivity
import com.example.myapplication.util.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountActivity : AppCompatActivity() {
    val apiService = RetrofitClient.apiService
    private val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        //뒤로 가기
        val backButton: ImageButton = findViewById(R.id.button_back)
        backButton.setOnClickListener { finish() }

        //연동하기
        val linkContactButton: ImageButton = findViewById(R.id.linkButton)
        linkContactButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
            }else{
                showConfirmationDialog("연락처를\n연동하시겠습니까?"){
                    fetchAndMove()
                }
            }
        }

        // 연락처 연동 해제 버튼
        val unlinkContactsButton: ImageButton = findViewById(R.id.unlinkButton)
        unlinkContactsButton.setOnClickListener {
            // 연락처 연동 해제 확인 다이얼로그
            showConfirmationDialog("연락처 연동을\n해제하시겠습니까?") {
                unlinkContacts()
            }
        }

        // 계정 탈퇴 버튼
        val deleteAccountButton: ImageButton = findViewById(R.id.deactivateButton)
        deleteAccountButton.setOnClickListener {
            // 계정 탈퇴 확인 다이얼로그
            showConfirmationDialog("탈퇴하시겠습니까?") {
                deleteAccount()
            }
        }
    }
    private fun fetchAndMove(){
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
                        Toast.makeText(this@AccountActivity, "$friendNum" +"명 연동 성공", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@AccountActivity, "$serverMsg", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    // 예: "Database error" 처리
                    Toast.makeText(this@AccountActivity, "연동 실패: $errorMessage", Toast.LENGTH_LONG).show()
                }
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

    private fun unlinkContacts() {
        CoroutineScope(Dispatchers.IO).launch{
            val response = apiService.unlink()
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    // 연락처 연동 해제 로직
                    Toast.makeText(this@AccountActivity, "연락처 연동 해제되었습니다.", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@AccountActivity, "연락처 연동 해제 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteAccount() {
        //preference 관리해야 함:
        //token 유무, 연락처 연동 유무
        CoroutineScope(Dispatchers.IO).launch{
            val response = apiService.deactivate()
            withContext(Dispatchers.Main){
                if(response.isSuccessful){
                    //sharedPreferences 삭제
                    val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear() // 모든 데이터를 삭제
                    editor.apply()

                    Toast.makeText(this@AccountActivity, "탈퇴되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AccountActivity,RegisterOrNotActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this@AccountActivity, "오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showConfirmationDialog(message: String, onConfirm: () -> Unit) {
        // AlertDialog로 확인 다이얼로그 표시
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_account, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_message)
        textView.text=message

        val dialog = builder.create()
        dialog.show()

        val positiveButton = dialogView.findViewById<Button>(R.id.button_yes)
        val negativeButton = dialogView.findViewById<Button>(R.id.button_no)

        positiveButton.setOnClickListener {
            onConfirm()  // onConfirm 콜백 호출
            dialog.dismiss()  // 다이얼로그 닫기
        }

        // 부정 버튼 클릭 시 다이얼로그만 닫기
        negativeButton.setOnClickListener {
            dialog.dismiss()  // 다이얼로그 닫기
        }
    }
}