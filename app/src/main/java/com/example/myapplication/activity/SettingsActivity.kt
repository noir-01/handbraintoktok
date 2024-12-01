package com.example.myapplication.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.util.SettingUtil


class SettingsActivity : AppCompatActivity() {
    private lateinit var soundVolumeSeekBar:SeekBar
    private lateinit var textSizeSeekBar:SeekBar
    private lateinit var soundVolumeValue:TextView
    private lateinit var textSizeValue:TextView
    private lateinit var saveSettingsButton:Button

    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        soundVolumeSeekBar = findViewById(R.id.soundVolumeSeekBar)
        textSizeSeekBar = findViewById(R.id.textSizeSeekBar)
        soundVolumeValue = findViewById(R.id.soundVolumeValue)
        textSizeValue = findViewById(R.id.textSizeValue)
        saveSettingsButton = findViewById(R.id.saveSettingsButton)

        //SharedPreferences에서 기존 설정 값 로드
        loadSettings()

        soundVolumeSeekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingUtil.setSoundVolume(applicationContext, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        //글자 크기 SeekBar 변경 리스너
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SettingUtil.setTextSize(applicationContext, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveSettingsButton.setOnClickListener {
            saveSettings()
            finish()
        }
    }

    private fun loadSettings(){
        val soundVolume = sharedPreferences.getInt("soundVolume", 50)
        val textSize = sharedPreferences.getInt("textSize", 20)

        soundVolumeSeekBar.progress = soundVolume
        textSizeSeekBar.progress = textSize

        soundVolumeValue.text = soundVolume.toString()
        textSizeValue.text = "${textSize}sp"
    }
    private fun saveSettings(){
        val editor = sharedPreferences.edit()
        editor.putInt("soundVolume", soundVolumeSeekBar.progress)
        editor.putInt("textSize", textSizeSeekBar.progress)
        editor.apply()
    }
}