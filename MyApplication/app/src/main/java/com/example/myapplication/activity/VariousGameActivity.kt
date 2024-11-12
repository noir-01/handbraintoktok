package com.example.myapplication.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.GameStartActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class VariousGameActivity : AppCompatActivity() {

    private var selectedGameName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_various_game)
    }

    private val randomNumber = (1..4).random()

    fun onButtonClick(view: View){
        selectedGameName = when(randomNumber){
            1 -> "mimic"
            2 -> "rps"
            3 -> "bwf"
            4 -> "random"
            else -> "Unknown Button"
        }

        if(allPermissionGranted()){
            startGame()
        }
        else{
            requestCameraPermission()
        }
    }

    private fun startGame(){
        val intent = Intent(this, GameStartActivity::class.java)
        intent.putExtra("GAME_NAME", selectedGameName)
        startActivity(intent)
    }

    private fun allPermissionGranted(): Boolean{
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission(){
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){isGranted: Boolean ->
        if(isGranted){
            startGame()
        }
        else{
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show()
        }
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}