package com.vtrifidgames.simplemindfulnesstimer.utils

import android.content.Context
import android.media.MediaPlayer
import com.vtrifidgames.simplemindfulnesstimer.R

class BellPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playBell() {
        // Release any existing player
        mediaPlayer?.release()
        // Create a new MediaPlayer using the bell sound resource.
        mediaPlayer = MediaPlayer.create(context, R.raw.bell_sound)
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
        }
        mediaPlayer?.start()
    }
}

