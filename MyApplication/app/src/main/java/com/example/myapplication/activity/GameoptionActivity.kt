package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.activity.game.DoggyActivity
import com.example.myapplication.activity.game.GameStartActivity
import com.example.myapplication.activity.game.RhythmGameSelectActivity

class GameoptionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_option)

        val backButton=findViewById<ImageButton>(R.id.button_back)

        val puppygameButton=findViewById<ImageButton>(R.id.button_puppy_game)
        val rhythmgameButton=findViewById<ImageButton>(R.id.button_rhythm_game)
        val randomgameButton=findViewById<ImageButton>(R.id.button_random_game)
        val copygameButton=findViewById<ImageButton>(R.id.button_copy_game)
        val rspgameButton=findViewById<ImageButton>(R.id.button_rsp_game)
        val calcgameButton=findViewById<ImageButton>(R.id.button_calc_game)

        //val togetherButton=findViewById<ImageButton>(R.id.button_together)

        backButton.setOnClickListener {
          finish()
        }
        puppygameButton.setOnClickListener {
            val intent=Intent(this, DoggyActivity::class.java)
            startActivity(intent)
        }
         
        rhythmgameButton.setOnClickListener {
            val intent=Intent(this, RhythmGameSelectActivity::class.java)
            startActivity(intent)
        }
        randomgameButton.setOnClickListener {
            val intent = Intent(this, GameStartActivity::class.java)
            intent.putExtra("MODE", "RANDOM")
            startActivity(intent)
        }
        copygameButton.setOnClickListener {
            val intent = Intent(this, GameStartActivity::class.java)
            intent.putExtra("MODE", "COPY")
            startActivity(intent)
        }
        rspgameButton.setOnClickListener {
            val intent = Intent(this, GameStartActivity::class.java)
            intent.putExtra("MODE", "RSP")
            startActivity(intent)
        }
        calcgameButton.setOnClickListener {
            val intent = Intent(this, GameStartActivity::class.java)
            intent.putExtra("MODE", "CALC")
            startActivity(intent)
        }
    }
}