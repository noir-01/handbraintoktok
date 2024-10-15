package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : BaseActivity() {

    private var selectedGameName: String? = null

    private var randomNumber : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permission granted for camera", Toast.LENGTH_LONG).show()
        }
        else{
            Toast.makeText(this, "Permission denied for camera", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(Manifest.permission.CAMERA),
                100
            )
        }

        val startGameButton = findViewById<Button>(R.id.button_start_game)
        val variousGameButton = findViewById<Button>(R.id.button_various_game)
        val recordsButton = findViewById<Button>(R.id.button_records)
        val methodButton = findViewById<Button>(R.id.button_method)
        val settingsButton = findViewById<Button>(R.id.button_settings)
        val exitButton = findViewById<Button>(R.id.button_exit)

        startGameButton.setOnClickListener{
            val intent = Intent(this, GameStartActivity::class.java) //GameActivity로 이동
            intent.putExtra("GAME_NAME", "mimic")
            startActivity(intent)
        }

        variousGameButton.setOnClickListener{
            randomNumber = (1..4).random()

            selectedGameName = when(randomNumber){
                1 -> "mimic"
                2 -> "rps"
                3 -> "bwf"
                4 -> "random"
                else -> "Unknown Button"
            }

            if(allPermissionGranted()){
                val intent = Intent(this, GameStartActivity::class.java) //GameActivity로 이동
                intent.putExtra("GAME_NAME", selectedGameName)
                startActivity(intent)
            }
            else{
                requestCameraPermission()
            }
        }

        recordsButton.setOnClickListener{
            val intent = Intent(this, RecordsActivity::class.java)
            startActivity(intent)
        }

        methodButton.setOnClickListener{
            val intent = Intent(this, MethodActivity::class.java) //GameActivity로 이동
            startActivity(intent)
        }

        settingsButton.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        exitButton.setOnClickListener{
            finish()    //현재 액티비티 종료
            System.exit(0)  //완전 종료
        }
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
        if(!isGranted){
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            finish()
        }
        else{
            selectedGameName = when(randomNumber) {
                1 -> "mimic"
                2 -> "rps"
                3 -> "bwf"
                4 -> "random"
                else -> "Unknown Button"
            }
            val intent = Intent(this, GameStartActivity::class.java)
            intent.putExtra("GAME_NAME", selectedGameName)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permission granted for camera", Toast.LENGTH_LONG).show()
        }
        else{
            Toast.makeText(this, "Permission denied for camera", Toast.LENGTH_LONG).show()
        }
    }
}