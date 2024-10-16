package com.example.myapplication.util

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.R

object SettingUtil{

    private fun getPreferences(context: Context):SharedPreferences{
        return context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    }

    fun getTextSize(context:Context):Int{
        return getPreferences(context).getInt("textSize", 20) //기본 값: 20sp
    }
    fun setTextSize(context:Context, size: Int){
        getPreferences(context).edit().putInt("textSize", size).apply()
        SettingsLiveData.updateTextSize(size)
    }

    fun getSoundVolume(context:Context):Int{
        return getPreferences(context).getInt("soundVolume", 50)
    }
    fun setSoundVolume(context: Context, volume: Int){
        getPreferences(context).edit().putInt("soundVolume", volume).apply()
        SettingsLiveData.updateSoundVolume(volume)
    }
}

object SettingsLiveData{

    private val _textSize = MutableLiveData<Int>()
    private val _soundVolume = MutableLiveData<Int>()

    val textSize: LiveData<Int> get() = _textSize
    val soundVolume: LiveData<Int> get() = _soundVolume

    fun updateTextSize(size: Int){
        _textSize.value = size
    }

    fun updateSoundVolume(volume: Int){
        _soundVolume.value = volume
    }

    fun initializeSettings(textSize: Int, soundVolume: Int){
        _textSize.value = textSize
        _soundVolume.value = soundVolume
    }
}

object ResourceUtils {
    val handImages = arrayOf(
        R.drawable.hand_0,
        R.drawable.hand_1,
        R.drawable.hand_2,
        R.drawable.hand_3,
        R.drawable.hand_4,
        R.drawable.hand_5,
        R.drawable.hand_6,
        R.drawable.hand_7,
        R.drawable.hand_8,
        R.drawable.hand_9,
        R.drawable.hand_10,
        R.drawable.hand_11,
        R.drawable.hand_12,
        R.drawable.hand_13,
        R.drawable.hand_14,
        R.drawable.hand_15,
        R.drawable.hand_16,
        R.drawable.hand_17,
        R.drawable.hand_18,
        R.drawable.hand_19,
        R.drawable.hand_20,
        R.drawable.hand_21
    )
}
