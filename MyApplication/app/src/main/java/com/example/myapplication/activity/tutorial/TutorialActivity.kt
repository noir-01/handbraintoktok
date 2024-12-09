package com.example.myapplication.activity.tutorial

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.activity.game.GameStartActivity

class TutorialActivity: AppCompatActivity() {
    private var hasPlayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_option)
        val doggyButton = findViewById<ImageButton>(R.id.button_puppy_game)
        val rhythmButton = findViewById<ImageButton>(R.id.button_rhythm_game)
        val randomButton = findViewById<ImageButton>(R.id.button_random_game)
        val copyButton = findViewById<ImageButton>(R.id.button_copy_game)
        val rspButton = findViewById<ImageButton>(R.id.button_rsp_game)
        val calcButton = findViewById<ImageButton>(R.id.button_calc_game)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)

        val backButton=findViewById<ImageButton>(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
        val buttons = listOf(copyButton,rspButton,calcButton,randomButton,doggyButton,rhythmButton)
        //copyButton 위치로 이동
        scrollView.post{scrollView.smoothScrollTo(0,copyButton.top)}

        hasPlayed = hasPlayedTutorial(this)
        if(!hasPlayed){
            //강제 튜토리얼 스킵 or not 물어보기
            showConfirmationDialog("게임 방법을\n알고 계신가요?",
                onYes={
                    tutorialFirstFinish(this@TutorialActivity)
                    hasPlayed=true
                    //알고 있으면 바로 main으로 이동
                    val intent = Intent(this@TutorialActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                //게임 방법을 모른다고 응답하면 모든 튜토리얼 진행하기
                onNo={
                    disableOtherButtons(buttons)
                    //1. 따라하기 게임부터 시작
                    copyButton.isEnabled=true
                    copyButton.alpha=1f
                    copyButton.clearColorFilter()
                }
            )
        }

        copyButton.setOnClickListener{
            val intent = Intent(this, TutorialGameActivity::class.java)
            intent.putExtra("MODE","COPY")
            startActivity(intent)

            if(!hasPlayed) {
                enableButton(copyButton,rspButton)
                scrollView.post{scrollView.smoothScrollTo(0,rspButton.top)}
            }
        }
        //이기기,지기 두문제
        rspButton.setOnClickListener{
            val intent = Intent(this, TutorialGameActivity::class.java)
            intent.putExtra("MODE","RSP")
            startActivity(intent)

            if(!hasPlayed) {
                enableButton(rspButton,calcButton)
                scrollView.post{scrollView.smoothScrollTo(0,calcButton.top)}
            }
        }
        calcButton.setOnClickListener{
            val intent = Intent(this, TutorialGameActivity::class.java)
            intent.putExtra("MODE","CALC")
            startActivity(intent)

            if(!hasPlayed) {
                enableButton(calcButton,randomButton)
                scrollView.post{scrollView.smoothScrollTo(0,randomButton.top)}
            }
        }
        randomButton.setOnClickListener{
            val intent = Intent(this, TutorialGameActivity::class.java)
            intent.putExtra("MODE","RANDOM")
            startActivity(intent)

            if(!hasPlayed) {
                enableButton(randomButton,doggyButton)
                scrollView.post{scrollView.smoothScrollTo(0,doggyButton.top)}
            }
        }
        doggyButton.setOnClickListener {
            val intent = Intent(this, TutorialImageActivity::class.java)
            intent.putExtra("MODE","DOGGY")
            startActivity(intent)

            if(!hasPlayed) {
                enableButton(doggyButton,rhythmButton)
                scrollView.post{scrollView.smoothScrollTo(0,rhythmButton.top)}
            }
        }
        rhythmButton.setOnClickListener {
            var intent = Intent(this, TutorialImageActivity::class.java)
            intent.putExtra("MODE","RHYTHM")
            if(!hasPlayed){
                //리듬게임 튜토리얼 보고 끝나면 메인으로 이동
                intent.putExtra("FIRST",true)
                moveToRhythmTutorialActivityThenMain.launch(intent)
            }else{
                startActivity(intent)
            }
        }
    }

    private val moveToRhythmTutorialActivityThenMain = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun disableOtherButtons(buttons: List<View>) {
        for (button in buttons) {
            if (button is ImageButton) {
                button.isEnabled = false
                button.alpha = 0.5f // 비활성화된 버튼은 투명도를 낮춰서 어둡게 보이게
                button.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY) // 이미지에 회색 필터 적용
            }
        }
    }
    private fun enableButton(button1:ImageButton, button2:ImageButton){
        button1.isEnabled=false
        button1.alpha=0.5f
        button1.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)

        button2.isEnabled=true
        button2.alpha=1f
        button2.clearColorFilter()
    }

    fun hasPlayedTutorial(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val hasPlayedTutorial = sharedPreferences.getBoolean("hasPlayedTutorial", false)

        return hasPlayedTutorial
    }
    fun tutorialFirstFinish(context: Context){
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        // 최초 실행인 경우, isFirstTime을 false로 변경하고 SharedPreferences에 저장
        val editor = sharedPreferences.edit()
        editor.putBoolean("hasPlayedTutorial", true)
        editor.apply()
    }
    private fun showConfirmationDialog(message: String,
                                       onYes: () -> Unit, onNo:()->Unit) {
        // AlertDialog로 확인 다이얼로그 표시
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_account, null)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(dialogView)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_message)
        textView.text=message

        val dialog = builder.create()
        dialog.show()

        val positiveButton = dialogView.findViewById<Button>(R.id.button_yes)
        val negativeButton = dialogView.findViewById<Button>(R.id.button_no)

        positiveButton.setOnClickListener {
            onYes()  // onConfirm 콜백 호출
            dialog.dismiss()  // 다이얼로그 닫기
        }

        // 부정 버튼 클릭 시 다이얼로그만 닫기
        negativeButton.setOnClickListener {
            onNo()
            dialog.dismiss()  // 다이얼로그 닫기
        }
    }
}