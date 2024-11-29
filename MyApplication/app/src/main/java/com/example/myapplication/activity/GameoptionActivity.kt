package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.activity.game.GameStartActivity
import com.example.myapplication.activity.game.RhythmGameSelectActivity

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

        randomgameButton.setOnClickListener {
            val intent = Intent(this, GameStartActivity::class.java)
            intent.putExtra("MODE", "RANDOM")
            startActivity(intent)
        }
        //따라하기 게임 테스트용
        togetherButton.setOnClickListener {
            val intent=Intent(this,GameStartActivity::class.java)
            intent.putExtra("MODE", "COPY")
            startActivity(intent)
        }

         
        rhythmgameButton.setOnClickListener {
            val intent=Intent(this, RhythmGameSelectActivity::class.java)
            startActivity(intent)
        }
    }
}