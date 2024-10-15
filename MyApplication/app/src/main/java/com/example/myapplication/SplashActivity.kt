package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // View Binding 사용 설정
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 페이드 아웃 애니메이션 생성
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 1000 // 페이드 아웃 시간 (밀리초)
            startOffset = 2000 // 애니메이션 시작 전 대기 시간 (밀리초)
            fillAfter = true // 애니메이션이 끝난 후 유지
        }

        // 애니메이션 리스너 설정 (애니메이션 종료 후 메인 화면으로 전환)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                // 메인 액티비티로 전환
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish() // 현재 액티비티 종료
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // 로고에 애니메이션 적용
        binding.logoImageView.startAnimation(fadeOut)
    }
}
