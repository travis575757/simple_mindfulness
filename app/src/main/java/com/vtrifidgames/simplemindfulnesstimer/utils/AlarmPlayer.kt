package com.vtrifidgames.simplemindfulnesstimer.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.vtrifidgames.simplemindfulnesstimer.R

class AlarmPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playAlarm() {
        // Get the default alarm URI. If it's not set, you might fallback to a bundled sound.
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        // Release any existing player.
        mediaPlayer?.release()

        // Create a new MediaPlayer instance.
        mediaPlayer = MediaPlayer().apply {
            // Set audio attributes to use the alarm usage.
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, alarmUri)
            prepare()
            start()
        }
    }

    fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
