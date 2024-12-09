package com.example.myapplication.activity.tutorial

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class TutorialImageActivity: AppCompatActivity()  {
    private lateinit var images: MutableList<Int>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial_image)
        images = mutableListOf<Int>()
        val mode = intent.getStringExtra("MODE")
        val firstTutorial = intent.getBooleanExtra("FIRST",false)

        when(mode){
            "RHYTHM"->{
                images = mutableListOf(
                    R.drawable.tutorial_rhythm_1,
                    R.drawable.tutorial_rhythm_2,
                    R.drawable.tutorial_rhythm_3,
                )
                if(firstTutorial) images.add(R.drawable.tutorial_rhythm_4)
            }
            "DOGGY"->{
                images = mutableListOf(
                    R.drawable.tutorial_doggy_feed_1,
                    R.drawable.tutorial_doggy_feed_2,
                    R.drawable.tutorial_doggy_bag_1,
                    R.drawable.tutorial_doggy_bag_2,
                    R.drawable.tutorial_doggy_bag_3,
                    R.drawable.tutorial_doggy_todo_1,
                    R.drawable.tutorial_doggy_todo_2,
                )

            }

        }
        
        val tutorialImageView = findViewById<ImageView>(R.id.tutorialImageView)
        val nextButton = findViewById<Button>(R.id.nextButton)
        val beforeButton = findViewById<Button>(R.id.beforeButton)
        var currentIndex = 0

        // 첫 번째 이미지 설정
        tutorialImageView.setImageResource(images[currentIndex])
        nextButton.visibility= View.VISIBLE
        nextButton.bringToFront()
        // 'Next' 버튼 클릭 이벤트
        nextButton.setOnClickListener {
            if (currentIndex < images.size - 1) {
                currentIndex++
                tutorialImageView.setImageResource(images[currentIndex])
            } else {
                // 마지막 이미지까지 보면 종료
                nextButton.isEnabled = false
                nextButton.visibility= View.GONE
                beforeButton.visibility= View.GONE
                tutorialImageView.visibility= View.GONE
                if(firstTutorial){
                    setResult(Activity.RESULT_OK)
                }
                finish()
            }
        }
        beforeButton.visibility= View.VISIBLE
        beforeButton.bringToFront()
        // 'Next' 버튼 클릭 이벤트
        beforeButton.setOnClickListener {
            if (currentIndex > -1) {
                currentIndex--
                tutorialImageView.setImageResource(images[currentIndex])
            }
        }
    }

}