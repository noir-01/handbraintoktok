package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import com.example.myapplication.activity.game.GameStartActivity

class MainActivity : BaseActivity() {
    private var selectedGameName: String? = null
    private var randomNumber : Int? = null
    private lateinit var buttonStartGame: ImageButton
    private lateinit var buttonRecords: ImageButton
    private lateinit var buttonMethod: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize buttons
        buttonStartGame=findViewById(R.id.button_start_game)
        buttonRecords = findViewById(R.id.button_records)
        buttonMethod = findViewById(R.id.button_method)


        // Set up button click listeners
        buttonStartGame.setOnClickListener {
            val intent = Intent(this, GameoptionActivity::class.java)
            startActivity(intent)
        }

        buttonRecords.setOnClickListener{
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
        }
        buttonMethod.setOnClickListener {
            val intent = Intent(this, MethodActivity::class.java)
            startActivity(intent)
        }
        // Optionally, request camera permission if needed
        if (!allPermissionGranted()) {
            requestCameraPermission()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })

    }

    private fun showExitConfirmationDialog() {
        // AlertDialog 생성
        AlertDialog.Builder(this)
            .setTitle("앱 종료")
            .setMessage("정말 종료하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                // 앱 종료
                finishAffinity()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                // 팝업 닫기
                dialog.dismiss()
            }
            .show()
    }

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
//            val intent = Intent(this, GameStartActivity::class.java)
//            intent.putExtra("GAME_NAME", selectedGameName)
//            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted for camera", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Permission denied for camera", Toast.LENGTH_LONG).show()
        }
    }
}
