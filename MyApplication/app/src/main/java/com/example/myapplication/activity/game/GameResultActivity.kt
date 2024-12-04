package com.example.myapplication.activity.game

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.GameoptionActivity
import com.example.myapplication.R
import com.example.myapplication.RecordActivity

class GameResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        // 반응 속도 예시 값
        val reactionSpeed = intent.getIntExtra("REACTION",0)

        val reactionSpeedText: TextView = findViewById(R.id.reactionSpeedText)
        val gameOptionButton = findViewById<Button>(R.id.selectGameButton)
        val recordButton = findViewById<Button>(R.id.viewRecordsButton)

        gameOptionButton.setOnClickListener{
            val intent = Intent(this, GameoptionActivity::class.java)
            startActivity(intent)
            finish()
        }

        recordButton.setOnClickListener {
            val intent = Intent(this,RecordActivity::class.java)
            startActivity(intent)
            finish()
        }



        val text = "반응속도  $reactionSpeed ms"
        val spannableString = SpannableString(text)

        // "반응속도:" 부분은 검정색
        val blackColorSpan = ForegroundColorSpan(getColor(android.R.color.black))
        spannableString.setSpan(blackColorSpan, 0, "반응속도:".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // "1200 ms" 부분은 빨간색
        val redColorSpan = ForegroundColorSpan(getColor(android.R.color.holo_red_light))
        spannableString.setSpan(redColorSpan, "반응속도: ".length, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        reactionSpeedText.text = spannableString
    }
}
