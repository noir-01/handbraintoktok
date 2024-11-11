package com.example.myapplication.activity

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.R

class MainActivity : BaseActivity() {
    private var selectedGameName: String? = null
    private var randomNumber : Int? = null
    private lateinit var buttonStartGame: ImageButton
    private lateinit var buttonVariousGame: ImageButton
    private lateinit var buttonRecords: ImageButton
    private lateinit var buttonMethod: ImageButton
    private lateinit var buttonSettings: ImageButton
    private lateinit var buttonHome1:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonStartGame = findViewById(R.id.button_start_game)
        buttonVariousGame = findViewById(R.id.button_various_game)
        buttonRecords = findViewById(R.id.button_records)
        buttonMethod = findViewById(R.id.button_method)
        buttonSettings = findViewById(R.id.button_settings)


        // ImageButton 크기 적용
        //adjustImageButtonSize()

        // 클릭 리스너 설정
        buttonStartGame.setOnClickListener {
            val intent = Intent(this, GameStartActivity::class.java)
            intent.putExtra("GAME_NAME", "mimic")
            startActivity(intent)
        }

        buttonVariousGame.setOnClickListener {
            val intent = Intent(this, VariousGameActivity::class.java)
            startActivity(intent)
        }

        buttonRecords.setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
        }

        buttonMethod.setOnClickListener {
            val intent = Intent(this, MethodActivity::class.java)
            startActivity(intent)
        }

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }


    }
/*
    override fun onResume() {
        super.onResume()
        adjustImageButtonSize()
    }

    private fun adjustImageButtonSize() {
        // SharedPreferences에서 크기 값 불러오기
        val buttonSize = sharedPreferences.getInt("button_size", 100)

        // ImageButton 크기 설정
        val layoutParams1 = buttonStartGame.layoutParams
        val layoutParams2 = buttonVariousGame.layoutParams
        val layoutParams3 = buttonRecords.layoutParams
        val layoutParams4 = buttonMethod.layoutParams
        val layoutParams5 = buttonSettings.layoutParams
        layoutParams1.width = buttonSize
        layoutParams1.height = buttonSize
        layoutParams2.width = buttonSize
        layoutParams2.height = buttonSize
        layoutParams3.width = buttonSize
        layoutParams3.height = buttonSize
        layoutParams4.width = buttonSize
        layoutParams4.height = buttonSize
        layoutParams5.width = buttonSize
        layoutParams5.height = buttonSize

        buttonStartGame.layoutParams = layoutParams1
        buttonVariousGame.layoutParams = layoutParams2
        buttonRecords.layoutParams = layoutParams3
        buttonMethod.layoutParams = layoutParams4
        buttonSettings.layoutParams = layoutParams5
    }

 */

    private fun allPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            selectedGameName = when (randomNumber) {
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
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted for camera", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Permission denied for camera", Toast.LENGTH_LONG).show()
        }
    }
}
