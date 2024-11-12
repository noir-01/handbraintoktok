package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MethodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_method)

        val homeButton=findViewById<ImageButton>(R.id.button_home)


        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val buttonSize = sharedPreferences.getInt("buttonSize", 70)

        homeButton.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }


        findViewById<ImageButton>(R.id.button_install).setOnClickListener {
            val intent = Intent(this, InstallActivity::class.java)
            startActivity(intent)
        }
        findViewById<ImageButton>(R.id.button_tutorial).setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
        }

    }
}