package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Visibility
import com.example.myapplication.util.dataClass.NumDto
import com.example.myapplication.util.dataClass.VerificationRequest
import com.example.myapplication.util.network.ApiService
import com.example.myapplication.util.network.RetrofitClient
import com.example.myapplication.util.network.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    val tokenManager = RetrofitClient.getTokenManager()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //////////////////////////////////////////////////////////////
        // RegisterOrNotActivity에서 Extra로 mode(Register/Auth) 전달

        val mode = intent.getStringExtra("mode")

        //////////////////////////////////////////////////////////////


        val phoneEditText = findViewById<EditText>(R.id.phoneEditText)
        val verifyButton = findViewById<Button>(R.id.verifyButton)
        val otpEditText = findViewById<EditText>(R.id.otpEditText)
        val startButton = findViewById<Button>(R.id.startButton)
        val nameText = findViewById<EditText>(R.id.nameEditText)

        //Auth에선 이름 칸 안보이게
        if(mode=="Auth") nameText.visibility= View.GONE

        val serverDomain = getString(R.string.server_domain)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://$serverDomain")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)


        // 전화번호 입력 시 11글자인지 확인
        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                verifyButton.isEnabled = s?.length == 11 // 11글자 입력 시 활성화
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 인증하기 버튼 클릭 시 인증번호 입력 칸과 시작하기 버튼 활성화
        verifyButton.setOnClickListener {
            val phoneNumber = phoneEditText.text.toString()
            CoroutineScope(Dispatchers.IO).launch {
                val response = apiService.sendSms(NumDto(phoneNumber))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "인증번호 전송됨", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RegisterActivity, "전송 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            otpEditText.isEnabled = true
            startButton.isEnabled = true
        }

        startButton.setOnClickListener{
            val phoneNumber = phoneEditText.text.toString()
            val code = otpEditText.text.toString()
            val name = nameText.text.toString()
            lateinit var response: Response<Map<String, Any>>
            CoroutineScope(Dispatchers.IO).launch {
                when(mode){
                    "Register" ->{
                        response = apiService.verifyCode(
                            VerificationRequest(phoneNumber, code, name)
                        )
                    }
                    "Auth" ->{
                        response = apiService.verifyRefresh(
                            VerificationRequest(phoneNumber, code, "")
                        )
                    }
                }

                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val token = responseBody?.get("token")?.toString() // token 값 추출
                        Log.d("token Recieved","$token");
                        token?.let {
                            //성공하면 토큰 저장하고 메인화면으로 넘어가기
                            tokenManager.saveToken(it)
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                        } ?: run {
                            Toast.makeText(this@RegisterActivity, "토큰을 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "인증 실패!", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }
}