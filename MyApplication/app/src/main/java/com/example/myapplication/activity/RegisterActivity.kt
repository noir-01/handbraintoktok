package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val phoneEditText = findViewById<EditText>(R.id.phoneEditText)
        val verifyButton = findViewById<Button>(R.id.verifyButton)
        val otpEditText = findViewById<EditText>(R.id.otpEditText)
        val startButton = findViewById<Button>(R.id.startButton)

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
            otpEditText.isEnabled = true
            startButton.isEnabled = true
        }
    }
}