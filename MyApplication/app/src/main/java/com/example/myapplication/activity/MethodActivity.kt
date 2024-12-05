package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.activity.tutorial.TutorialActivity

class MethodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_method)

        val backButton=findViewById<ImageButton>(R.id.button_back)


        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val buttonSize = sharedPreferences.getInt("buttonSize", 70)

        backButton.setOnClickListener{
            finish()
        }


        findViewById<ImageButton>(R.id.button_install).setOnClickListener {
            val intent = Intent(this, InstallActivity::class.java)
            startActivity(intent)
            //finish 넣어서 종료 시 메인으로 이동
            finish()
        }
        findViewById<ImageButton>(R.id.button_tutorial).setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
            //finish 넣어서 종료 시 메인으로 이동
            finish()
        }

    }
}