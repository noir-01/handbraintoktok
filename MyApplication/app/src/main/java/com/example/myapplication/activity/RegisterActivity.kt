package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.transition.Visibility
import com.example.myapplication.util.dataClass.NumDto
import com.example.myapplication.util.dataClass.VerificationRequest
import com.example.myapplication.util.network.ApiService
import com.example.myapplication.util.network.RetrofitClient
import com.example.myapplication.util.network.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.async


class RegisterActivity : AppCompatActivity() {

    val tokenManager = RetrofitClient.getTokenManager()
    val apiService = RetrofitClient.apiService
    
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //////////////////////////////////////////////////////////////
        // RegisterOrNotActivity에서 Extra로 mode(Register/Auth) 전달

        val mode = intent.getStringExtra("mode")
        
        //////////////////////////////////////////////////////////////


        val phoneEditText = findViewById<EditText>(R.id.phoneEditText)
        val verifyButton: ImageButton = findViewById(R.id.verifyButton)
        val otpEditText = findViewById<EditText>(R.id.otpEditText)
        val startButton = findViewById<Button>(R.id.startButton)
        val nameText = findViewById<EditText>(R.id.nameEditText)
        val labelYear = findViewById<TextView>(R.id.labelYear)
        val yearPicker = findViewById<NumberPicker>(R.id.yearPicker)
        var birthYear: Int = 2000

        //Auth에선 이름, 출생년도 칸 안보이게
        if(mode=="Auth") {
            nameText.visibility= View.GONE
            labelYear.visibility=View.GONE
            yearPicker.visibility=View.GONE
        }

        val serverDomain = getString(R.string.server_domain)

        //register=>main 넘어가는 조건을 이전에 처리하기
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isContactSyncDone = sharedPreferences.getBoolean("isContactSyncDone", false)


        fun startTimer(): Boolean {
            val countDownTimer = object : CountDownTimer(5000, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    //verifyButton.text = "${millisUntilFinished / 1000}초"
                }

                override fun onFinish() {
                    verifyButton.isEnabled = true
                    //verifyButton.text = "인증번호 전송"
                }
            }
            countDownTimer.start()
            return true
        }

        // 전화번호 입력 시 11글자인지 확인
        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                verifyButton.isEnabled = s?.length == 11 // 11글자 입력 시 활성화
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //생년 값 범위 설정
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        // NumberPicker 설정
        yearPicker.minValue = 1900  // 최소 연도
        yearPicker.maxValue = currentYear // 현재 연도까지 선택 가능
        yearPicker.value = currentYear // 기본값: 현재 연도

        yearPicker.setOnValueChangedListener { _, _, newVal ->
            birthYear=newVal
            //시작 버튼 활성화
            startButton.isEnabled = true
        }


        // 인증하기 버튼 클릭 시 인증번호 입력 칸과 시작하기 버튼 활성화
        verifyButton.setOnClickListener {
            val phoneNumber = phoneEditText.text.toString()
            // 버튼 비활성화
            verifyButton.isEnabled = false

            //비동기로 버튼 비활성화
            CoroutineScope(Dispatchers.Main).launch{
                val timerJob = launch { startTimer() }
            }

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
            //Auth에선 바로 활성화
            if(mode=="Auth"){
                startButton.isEnabled = true  
            }
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
                            VerificationRequest(phoneNumber, code, name, birthYear)
                        )
                    }
                    "Auth" ->{
                        response = apiService.verifyRefresh(
                            //같은 dto 사용, 서버에선 phoneNum과 code만 이용
                            VerificationRequest(phoneNumber, code, "",0)
                        )
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    if (response.isSuccessful) {
                        val intent: Intent
                        val responseBody = response.body()
                        val token = responseBody?.get("token")?.toString() // token 값 추출
                        Log.d("token Recieved","$token");
                        token?.let {
                            //성공하면 토큰+이름 저장하고 다음 화면(연락처 연동)으로 넘어가기
                            tokenManager.saveToken(it)

                            withContext(Dispatchers.IO){
                                //인증만 다시 했을 경우 앱이 이름을 모르고 있는 상태, sharedPreferences에 저장
                                val myName = getMyName()
                                Log.d("Register","$myName")
                                val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                                sharedPreferences.edit()
                                    .putString("userName", myName)
                                    .apply()
                            }
                        } ?: run {
                            Toast.makeText(this@RegisterActivity, "토큰을 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }

                        if(isContactSyncDone){
                            intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        }else{
                            intent = Intent(this@RegisterActivity, AddFriendActivity::class.java)
                        }
                        startActivity(intent)

                    } else {
                        var toastMessage = ""
                        when(response.code()){
                            401 -> toastMessage = "인증번호 틀림"
                            404 -> toastMessage = "회원가입 해주세요"
                            409 -> toastMessage = "이미 가입된 유저입니다"
                            500 -> toastMessage = "서버 오류, 잠시 후 다시 시도하세요"
                        }
                        Toast.makeText(this@RegisterActivity, toastMessage, Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

    }

    fun saveUserName(context: Context, userName: String) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_name", userName)
        editor.apply() // 비동기 저장
    }

    suspend fun getMyName(): String {
        var name = ""
        val response = apiService.getMyName()  // 비동기 작업
        if (response.isSuccessful) {
            name = response.body()?.get("name").toString()
        }
        return name
    }
}