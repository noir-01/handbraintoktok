package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.util.network.RetrofitClient
import android.Manifest
import android.provider.ContactsContract
import android.util.Log

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 200
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val tokenManager = RetrofitClient.getTokenManager()
        // 로그인 버튼을 찾아서 클릭 리스너 설정
        val loginButton: Button = findViewById(R.id.btn)
        loginButton.setOnClickListener {
            // 로그인 버튼 클릭 시 HomeActivity로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            tokenManager.saveToken("mytoken")

            finish() // 현재 LoginActivity를 종료
        }

        //권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS)
        }else{
            fetchContacts()
        }


    }

    //연락처 가져오기
    private fun fetchContacts() {
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
            val nameIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                if (nameIndex >= 0 && numberIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    val number = cursor.getString(numberIndex)
                    Log.d("Contacts", "Name: $name, Number: $number")
                }
            }
        }
    }
}
