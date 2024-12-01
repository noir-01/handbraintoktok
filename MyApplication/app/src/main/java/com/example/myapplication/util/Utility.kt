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
    val imageResources = mapOf(
        "hand_alien_l" to R.drawable.hand_alien_l,
        "hand_alien_r" to R.drawable.hand_alien_r,
        "hand_baby_l" to R.drawable.hand_baby_l,
        "hand_baby_r" to R.drawable.hand_baby_r,
        "hand_call_l" to R.drawable.hand_call_l,
        "hand_call_r" to R.drawable.hand_call_r,
        "hand_eight_l" to R.drawable.hand_eight_l,
        "hand_eight_r" to R.drawable.hand_eight_r,
        "hand_five_l" to R.drawable.hand_five_l,
        "hand_five_r" to R.drawable.hand_five_r,
        "hand_four_l" to R.drawable.hand_four_l,
        "hand_four_r" to R.drawable.hand_four_r,
        "hand_heart_l" to R.drawable.hand_heart_l,
        "hand_heart_r" to R.drawable.hand_heart_r,
        "hand_heart_twohands" to R.drawable.hand_heart_twohands,
        "hand_lucky_finger_l" to R.drawable.hand_lucky_finger_l,
        "hand_lucky_finger_r" to R.drawable.hand_lucky_finger_r,
        "hand_mandoo_l" to R.drawable.hand_mandoo_l,
        "hand_mandoo_r" to R.drawable.hand_mandoo_r,
        "hand_middle_finger_l" to R.drawable.hand_middle_finger_l,
        "hand_middle_finger_r" to R.drawable.hand_middle_finger_r,
        "hand_ok_l" to R.drawable.hand_ok_l,
        "hand_ok_r" to R.drawable.hand_ok_r,
        "hand_one_l" to R.drawable.hand_one_l,
        "hand_one_r" to R.drawable.hand_one_r,
        "hand_rabbit_l" to R.drawable.hand_rabbit_l,
        "hand_rabbit_r" to R.drawable.hand_rabbit_r,
        "hand_rock_l" to R.drawable.hand_rock_l,
        "hand_rock_r" to R.drawable.hand_rock_r,
        "hand_seven_l" to R.drawable.hand_seven_l,
        "hand_seven_r" to R.drawable.hand_seven_r,
        "hand_six_l" to R.drawable.hand_six_l,
        "hand_six_r" to R.drawable.hand_six_r,
        "hand_three_l" to R.drawable.hand_three_l,
        "hand_three_r" to R.drawable.hand_three_r,
        "hand_thumb_up_l" to R.drawable.hand_thumb_up_l,
        "hand_thumb_up_r" to R.drawable.hand_thumb_up_r,
        "hand_two_l" to R.drawable.hand_two_l,
        "hand_two_r" to R.drawable.hand_two_r,
        "hand_v_l" to R.drawable.hand_v_l,
        "hand_v_r" to R.drawable.hand_v_r,
        "hand_wolf_l" to R.drawable.hand_wolf_l,
        "hand_wolf_r" to R.drawable.hand_wolf_r,
        "num_1" to R.drawable.num_1,
        "num_2" to R.drawable.num_2,
        "num_3" to R.drawable.num_3,
        "num_4" to R.drawable.num_4,
        "num_5" to R.drawable.num_5,
        "num_6" to R.drawable.num_6,
        "num_7" to R.drawable.num_7,
        "num_8" to R.drawable.num_8,
        "num_9" to R.drawable.num_9,
        "num_10" to R.drawable.num_10
    )
}