package com.komkum.komkum.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayer

class MediaReceiver(var player: ExoPlayer) : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == p1?.action) {
            player.pause()
        }
    }
}
