package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.util.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterOrNotActivity : AppCompatActivity() {
    val tokenManager = RetrofitClient.getTokenManager()
    val apiService = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //토큰이 있으면 로그인 시도, 성공 시 메인화면으로 바로 이동
        if(tokenManager.getToken()!=null){
            CoroutineScope(Dispatchers.IO).launch{
                val response = apiService.login()
                withContext(Dispatchers.Main){
                    if(response.isSuccessful){
                        val intent = Intent(this@RegisterOrNotActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        initializeUI()
                    }
                }
            }
        }else{
            //토큰 없으면 바로 화면 띄우기
            initializeUI()
        }

    }
    private fun initializeUI(){
        setContentView(R.layout.activity_register_or_not)
        // 버튼 연결
        val btnSignup = findViewById<Button>(R.id.btn_signup)
        val btnAuthenticate = findViewById<Button>(R.id.btn_authenticate)

        // "예(회원가입)" 버튼 클릭 시 회원가입 화면으로 이동
        btnSignup.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("mode", "Register")
            startActivity(intent)
        }

        // "아니오(인증하기)" 버튼 클릭 시 인증 화면으로 이동
        btnAuthenticate.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("mode", "Auth")
            startActivity(intent)
        }
    }
}
