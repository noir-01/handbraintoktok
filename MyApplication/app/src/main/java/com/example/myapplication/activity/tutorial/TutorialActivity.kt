package com.example.myapplication.activity.tutorial

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.activity.game.GameStartActivity

class TutorialActivity: AppCompatActivity() {
    private var hasPlayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_option)
        val doggyButton = findViewById<ImageButton>(R.id.button_puppy_game)
        val rhythmButton = findViewById<ImageButton>(R.id.button_rhythm_game)
        val randomButton = findViewById<ImageButton>(R.id.button_random_game)
        val copyButton = findViewById<ImageButton>(R.id.button_copy_game)
        val rspButton = findViewById<ImageButton>(R.id.button_rsp_game)
        val calcButton = findViewById<ImageButton>(R.id.button_calc_game)

        val backButton=findViewById<ImageButton>(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
        hasPlayed = hasPlayedTutorial(this)

        val buttons = listOf(copyButton,rspButton,calcButton,randomButton,doggyButton,rhythmButton)

        //처음 시행한 유저면 강제로 모든 튜토리얼 수행
        if(!hasPlayed){
            disableOtherButtons(buttons)
            //1. 따라하기 게임부터 시작
            copyButton.isEnabled=true
            copyButton.alpha=1f
            copyButton.clearColorFilter()
        }
        copyButton.setOnClickListener{
            val intent = Intent(this, TutorialGameActivity::class.java)
            intent.putExtra("MODE","COPY")
            startActivity(intent)
            if(!hasPlayed) enableButton(copyButton,rspButton)
        }
        //이기기,지기 두문제
        rspButton.setOnClickListener{
            val intent = Intent(this, TutorialGameActivity::class.java)
            intent.putExtra("MODE","RSP")
            startActivity(intent)
            if(!hasPlayed) enableButton(rspButton,calcButton)
        }
        calcButton.setOnClickListener{
            val intent = Intent(this, TutorialGameActivity::class.java)
            intent.putExtra("MODE","CALC")
            startActivity(intent)
            if(!hasPlayed) enableButton(calcButton,randomButton)
        }
        randomButton.setOnClickListener{
            val intent = Intent(this, TutorialGameActivity::class.java)
            intent.putExtra("MODE","RANDOM")
            startActivity(intent)
            if(!hasPlayed) enableButton(randomButton,doggyButton)
        }
        doggyButton.setOnClickListener {
            val intent = Intent(this, TutorialImageActivity::class.java)
            intent.putExtra("MODE","DOGGY")
            startActivity(intent)
            if(!hasPlayed) enableButton(doggyButton,rhythmButton)
            //이후 튜토리얼 종료하기
        }
        rhythmButton.setOnClickListener {
            var intent = Intent(this, TutorialImageActivity::class.java)
            intent.putExtra("MODE","RHYTHM")
            startActivity(intent)
            if(!hasPlayed){
                tutorialFirstFinish(this)
                hasPlayed=true
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun disableOtherButtons(buttons: List<View>) {
        for (button in buttons) {
            if (button is ImageButton) {
                button.isEnabled = false
                button.alpha = 0.5f // 비활성화된 버튼은 투명도를 낮춰서 어둡게 보이게
                button.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY) // 이미지에 회색 필터 적용
            }
        }
    }
    private fun enableButton(button1:ImageButton, button2:ImageButton){
        button1.isEnabled=false
        button1.alpha=0.5f
        button1.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)

        button2.isEnabled=true
        button2.alpha=1f
        button2.clearColorFilter()
    }

    fun hasPlayedTutorial(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val hasPlayedTutorial = sharedPreferences.getBoolean("hasPlayedTutorial", false)

        return hasPlayedTutorial
    }
    fun tutorialFirstFinish(context: Context){
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        // 최초 실행인 경우, isFirstTime을 false로 변경하고 SharedPreferences에 저장
        val editor = sharedPreferences.edit()
        editor.putBoolean("hasPlayedTutorial", true)
        editor.apply()
    }
}