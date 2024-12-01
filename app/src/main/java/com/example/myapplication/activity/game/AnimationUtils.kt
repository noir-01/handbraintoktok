package com.example.myapplication.activity.game

import android.graphics.drawable.AnimationDrawable
import android.widget.ImageView
import com.example.myapplication.R


enum class ActionType(val delay: Long){
    WALK(3000L),
    RUN(1500L),
    JUMP(1000L),
    IDLE1(2000L),
    IDLE2(2000L),
    SIT(2000L),
    SNIFF(4000L),
    SNIFF_AND_WALK(3500L)
}

object AnimationUtils {
    fun startAnimation(imageView: ImageView, actionType: ActionType) {
        val animationRes = when(actionType){
            ActionType.WALK -> R.drawable.dog1_walk_animation
            ActionType.RUN -> R.drawable.dog1_run_animation
            ActionType.JUMP -> R.drawable.dog1_jump_animation
            ActionType.IDLE1 -> R.drawable.dog1_idle1_animation
            ActionType.IDLE2 -> R.drawable.dog1_idle2_animation
            ActionType.SIT -> R.drawable.dog_sit_animation
            ActionType.SNIFF -> R.drawable.dog1_sniff_animation
            ActionType.SNIFF_AND_WALK -> R.drawable.dog1_sniff_and_walk_animation
            else -> null
        }
        animationRes?.let {
            imageView.setImageDrawable(null)
            imageView.background = null

            imageView.setBackgroundResource(it)
            val animationDrawable = imageView.background as AnimationDrawable
            animationDrawable.isOneShot = false
            animationDrawable.start()
        }
    }

    fun stopAnimation(imageView: ImageView) {
        val animationDrawable = imageView.background as? AnimationDrawable
        animationDrawable?.stop()
    }

}
