package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.model.AlertTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundAlertHelper {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays the selected tone at the configured volume level with optional vibration.
     */
    fun playTone(
        context: Context,
        tone: AlertTone = AlertTone.CAMPANA,
        volume: Float = 0.85f,
        vibrate: Boolean = true
    ) {
        val volumePercent = (volume * 100).toInt().coerceIn(10, 100)

        scope.launch {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, volumePercent)
                when (tone) {
                    AlertTone.CAMPANA -> {
                        // Double bell acoustic ping
                        tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 180)
                        delay(200)
                        tg.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 280)
                    }

                    AlertTone.ALERTA_FUERTE -> {
                        // Loud emergency warning pulses
                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 220)
                        delay(120)
                        tg.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
                        delay(120)
                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
                    }

                    AlertTone.CHIME_IOS -> {
                        // Triad harmonic chime sequence (iOS-style notification)
                        tg.startTone(ToneGenerator.TONE_DTMF_0, 100)
                        delay(110)
                        tg.startTone(ToneGenerator.TONE_DTMF_5, 100)
                        delay(110)
                        tg.startTone(ToneGenerator.TONE_DTMF_B, 240)
                    }
                }
                delay(400)
                tg.release()
            } catch (e: Exception) {
                Log.w("SoundAlertHelper", "Error playing tone $tone", e)
            }
        }

        if (vibrate) {
            triggerVibration(context, tone)
        }
    }

    /**
     * Legacy helper method for backwards compatibility
     */
    fun playNewOrderAlert(context: Context) {
        playTone(context, AlertTone.CAMPANA, 0.85f, true)
    }

    private fun triggerVibration(context: Context, tone: AlertTone) {
        try {
            val timings = when (tone) {
                AlertTone.CAMPANA -> longArrayOf(0, 120, 80, 180)
                AlertTone.ALERTA_FUERTE -> longArrayOf(0, 200, 70, 200, 70, 300)
                AlertTone.CHIME_IOS -> longArrayOf(0, 80, 60, 80, 60, 120)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(timings, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (e: Exception) {
            Log.w("SoundAlertHelper", "Error triggering vibration", e)
        }
    }
}

