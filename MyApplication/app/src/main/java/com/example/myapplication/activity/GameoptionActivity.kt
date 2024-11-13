package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.activity.RhythmGameSelectActivity
import com.example.myapplication.activity.VariousGameActivity
import com.example.myapplication.multiUi.LoginActivity

class GameoptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameoption)

        val backButton=findViewById<ImageButton>(R.id.button_back)
        val randomgameButton=findViewById<ImageButton>(R.id.button_random_game)
        val rhythmgameButton=findViewById<ImageButton>(R.id.button_rhythm_game)
        val togetherButton=findViewById<ImageButton>(R.id.button_together)

        backButton.setOnClickListener {
          finish()
        }

        // 랜덤 게임 완성되면 수정 필요
        randomgameButton.setOnClickListener {
            val intent = Intent(this, GameStartActivity::class.java)
            intent.putExtra("GAME_NAME", "mimic")
            startActivity(intent)
        }
        togetherButton.setOnClickListener {
            val intent=Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        rhythmgameButton.setOnClickListener {
            val intent=Intent(this,RhythmGameSelectActivity::class.java)
            startActivity(intent)
        }
    }
}