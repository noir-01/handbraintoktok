package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.health.connect.datatypes.units.Volume
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

open class BaseActivity : AppCompatActivity(){
    protected lateinit var sharedPreferences:SharedPreferences

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        applySettings()
        observeSettingsChanges()
    }

    private fun applySettings(){
        val textSize = sharedPreferences.getInt("textSize", 20)
        val soundVolume = sharedPreferences.getInt("soundVolume", 50)

        applyTextSize(textSize)

        //val mediaPlayer = MediaPlayer.create(this, R.raw.sample_sound)
        //mediaPlayer.setVolume(soundVolume / 100f, soundVolume / 100f)
    }

    private fun observeSettingsChanges(){
        SettingsLiveData.textSize.observe(this, Observer { size ->
            applyTextSize(size)
        })

        SettingsLiveData.soundVolume.observe(this, Observer { volume ->
            applySoundVolume(volume)
        })
    }

    private fun applyTextSize(size: Int){
        val rootView = window.decorView.findViewById(android.R.id.content) as ViewGroup
        adjustTextSizeRecursively(rootView, size)
    }

    private fun applySoundVolume(volume: Int){
        //MediaPlayer가 초기화되지 않았을 경우, 초기화
        if (mediaPlayer == null){
            //mediaPlayer = MediaPlayer.create(this, R.raw.sample_sound)   //샘플 사운드
            mediaPlayer?.isLooping = true   //필요시 반복 재생
        }

        val volumeLevel = volume / 100f
        mediaPlayer?.setVolume(volumeLevel, volumeLevel)

        if(!mediaPlayer!!.isPlaying){
            mediaPlayer?.start()
        }
    }

    private fun adjustTextSizeRecursively(viewGroup: ViewGroup, size: Int){
        for (i in 0 until viewGroup.childCount){
            val child = viewGroup.getChildAt(i)
            if (child is TextView){
                child.textSize = size.toFloat()
            }
            else if (child is ViewGroup){
                adjustTextSizeRecursively(child, size)
            }
        }
    }
}