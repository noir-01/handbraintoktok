package com.example.myapplication

import android.Manifest
import android.app.ActivityOptions
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.util.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.provider.Settings
import android.widget.TextView

class RegisterOrNotActivity : AppCompatActivity() {
    val tokenManager = RetrofitClient.getTokenManager()
    val apiService = RetrofitClient.apiService

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
        )
    }

    private fun checkPermissions() {
        // 모든 필수 권한이 승인되었는지 확인
        val deniedPermissions = REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        // 거부된 권한이 있다면 권한 요청
        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                deniedPermissions.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // 모든 권한이 승인되었는지 확인
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allPermissionsGranted) {
                // 모든 권한 승인됨 - 앱 정상 진행
            } else {
                // 일부 또는 모든 권한 거부됨
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_account, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val textView = dialogView.findViewById<TextView>(R.id.dialog_message)

        val buttonYes = dialogView.findViewById<Button>(R.id.button_yes)

        val buttonNo = dialogView.findViewById<Button>(R.id.button_no)

        textView.text = "게임을 하기 위해\n권한이 필요합니다."
        buttonYes.text = "설정"
        buttonNo.text = "종료"

        buttonYes.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            dialog.dismiss()
            finish()
        }
        buttonNo.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        //토큰이 있으면 로그인 시도, 성공 시 메인화면으로 바로 이동
        if(tokenManager.getToken()!=null){
            CoroutineScope(Dispatchers.IO).launch{
                val response = apiService.login()
                withContext(Dispatchers.Main){
                    if(response.isSuccessful){
                        Log.d("token",tokenManager.getToken().toString())
                        val intent = Intent(this@RegisterOrNotActivity, MainActivity::class.java)
                        //val options = ActivityOptions.makeCustomAnimation(this@RegisterOrNotActivity, R.anim.fade_in, R.anim.fade_in)
                        //startActivity(intent,options.toBundle())
                        startActivity(intent)
                    }else{
                        //토큰 검증 실패 시(유효기간 초과 등)
                        initializeUI()
                    }
                }
            }
        }else{
            //토큰 없으면 바로 화면 띄우기
            initializeUI()
        }

    }
    private fun initializeUI(){
        setContentView(R.layout.activity_register_or_not)
        // 버튼 연결
        val btnSignup = findViewById<Button>(R.id.btn_signup)
        val btnAuthenticate = findViewById<Button>(R.id.btn_authenticate)

        // "예(회원가입)" 버튼 클릭 시 회원가입 화면으로 이동
        btnSignup.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("mode", "Register")
            startActivity(intent)
        }

        // "아니오(인증하기)" 버튼 클릭 시 인증 화면으로 이동
        btnAuthenticate.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("mode", "Auth")
            startActivity(intent)
        }
    }
}
