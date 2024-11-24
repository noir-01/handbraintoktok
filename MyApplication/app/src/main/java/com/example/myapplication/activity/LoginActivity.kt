package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.util.network.RetrofitClient

class LoginActivity : AppCompatActivity() {

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
    }
}
