package com.example.myapplication

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import com.example.myapplication.activity.AccountActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        val accountButton = findViewById<ImageButton>(R.id.button_account)


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
        accountButton.setOnClickListener {
            val intent = Intent(this,AccountActivity::class.java)
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_account, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        //취소 동작 X
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val textView = dialogView.findViewById<TextView>(R.id.dialog_message)
        textView.text="게임을\n종료할까요?"
        val buttonNo = dialogView.findViewById<Button>(R.id.button_no)
        val buttonYes = dialogView.findViewById<Button>(R.id.button_yes)
        // 예 버튼 클릭 시 동작
        buttonYes.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                dialog.dismiss()
                finishAffinity()
            }
        }
        //아니오 버튼
        buttonNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
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
