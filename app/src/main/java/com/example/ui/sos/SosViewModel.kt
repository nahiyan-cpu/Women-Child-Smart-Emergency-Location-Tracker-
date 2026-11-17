package com.example.ui.sos

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Incident
import com.example.data.repository.SafeGuardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SafeGuardRepository(application)

    private val _countdown = MutableStateFlow(5)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private var countdownJob: Job? = null
    private var alarmJob: Job? = null
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    init {
        val context = application.applicationContext
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        startSosCountdown()
    }

    private fun startSosCountdown() {
        countdownJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                _countdown.value = i
                delay(1000)
            }
            triggerFullSos()
        }
    }

    fun cancelSos() {
        countdownJob?.cancel()
        alarmJob?.cancel()
        stopAlarm()
        _isSosActive.value = false
    }

    private fun triggerFullSos() {
        _isSosActive.value = true
        // Log incident to Firestore
        viewModelScope.launch {
            try {
                repository.logIncident(
                    Incident(
                        latitude = 0.0, // Will be updated with real GPS if available
                        longitude = 0.0,
                        address = "Emergency SOS Triggered",
                        status = "ACTIVE",
                        notes = "Panic button pressed by user."
                    )
                )
            } catch (e: Exception) {
                // Ignore network errors during SOS to ensure local alarm sounds
            }
        }

        // Start siren & vibration loop
        alarmJob = viewModelScope.launch {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                while (_isSosActive.value) {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1500)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
                    }
                    delay(2000)
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    private fun stopAlarm() {
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelSos()
    }
}
