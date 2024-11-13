package com.example.myapplication.multiUi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCallBinding
import com.example.myapplication.service.MainService
import com.example.myapplication.service.MainServiceRepository
import com.example.myapplication.util.webrtc.convertToHumanTime
import com.example.myapplication.webrtc.RTCAudioManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class CallActivity : AppCompatActivity(), MainService.EndCallListener {

    private var target:String?=null
    private var isVideoCall:Boolean= true
    private var isCaller:Boolean = true

    private var isMicrophoneMuted = false
    private var isScreenCasting = false


    @Inject lateinit var serviceRepository: MainServiceRepository
    private lateinit var requestScreenCaptureLauncher:ActivityResultLauncher<Intent>

    private lateinit var views:ActivityCallBinding

    override fun onStart() {
        super.onStart()
        requestScreenCaptureLauncher = registerForActivityResult(ActivityResultContracts
            .StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                val intent = result.data
                //its time to give this intent to our service and service passes it to our webrtc client
                MainService.screenPermissionIntent = intent
                isScreenCasting = true
                updateUiToScreenCaptureIsOn()
                serviceRepository.toggleScreenShare(true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityCallBinding.inflate(layoutInflater)
        setContentView(views.root)
        init()
    }

    private fun init(){
        intent.getStringExtra("target")?.let {
            this.target = it
        }?: kotlin.run {
            finish()
        }

        isVideoCall = intent.getBooleanExtra("isVideoCall",true)
        isCaller = intent.getBooleanExtra("isCaller",true)

        views.apply {
            callTitleTv.text = "$target 와 게임중이에요"
            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0..3600){
                    delay(1000)
                    withContext(Dispatchers.Main){
                        //convert this int to human readable time
                        callTimerTv.text = i.convertToHumanTime()
                    }
                }
            }

            if (!isVideoCall){

            }
            MainService.remoteSurfaceView = remoteView
            MainService.localSurfaceView = localView
            serviceRepository.setupViews(isVideoCall,isCaller,target!!)

            endCallButton.setOnClickListener {
                serviceRepository.sendEndCall()
            }
        }
        setupMicToggleClicked()
        MainService.endCallListener = this
    }


    private fun updateUiToScreenCaptureIsOn(){
        views.apply {
            localView.isVisible = false
        }

    }
    private fun updateUiToScreenCaptureIsOff() {
        views.apply {
            localView.isVisible = true
        }
    }
    private fun setupMicToggleClicked(){
        views.apply {
            toggleMicrophoneButton.setOnClickListener {
                if (!isMicrophoneMuted){
                    //we should mute our mic
                    //1. send a command to repository
                    serviceRepository.toggleAudio(true)
                    //2. update ui to mic is muted
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_on)
                }else{
                    //we should set it back to normal
                    //1. send a command to repository to make it back to normal status
                    serviceRepository.toggleAudio(false)
                    //2. update ui
                    toggleMicrophoneButton.setImageResource(R.drawable.ic_mic_off)
                }
                isMicrophoneMuted = !isMicrophoneMuted
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        serviceRepository.sendEndCall()
    }


    override fun onCallEnded() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        MainService.remoteSurfaceView?.release()
        MainService.remoteSurfaceView = null

        MainService.localSurfaceView?.release()
        MainService.localSurfaceView =null

    }
}