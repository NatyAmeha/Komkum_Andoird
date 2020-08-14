package com.example.ethiomusic.media

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.os.bundleOf
import com.example.ethiomusic.data.model.Song
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator



 class PlaylistQueueNavigator(mediasession : MediaSessionCompat , var playbackPreparer: CustomPlaybackPreparer) : TimelineQueueNavigator(mediasession) {
    override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat {
        return getMediaDescription(playbackPreparer.playlistInfo[windowIndex] , windowIndex , player)
    }


    override fun getSupportedQueueNavigatorActions(player: Player?): Long {
        return  PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
    }


    override fun onSkipToNext(player: Player?, controlDispatcher: ControlDispatcher?) {
        player?.next()
        player?.playWhenReady = true
    }


    override fun onSkipToPrevious(player: Player?, controlDispatcher: ControlDispatcher?) {
        player?.previous()
        player?.playWhenReady = true

    }

    override fun onSkipToQueueItem(player: Player?, controlDispatcher: ControlDispatcher?, id: Long) {
        player?.seekTo(id.toInt() , 0)
        player?.playWhenReady = true
    }

     fun getMediaDescription(song : Song<String,String> , index : Int , player: Player?) : MediaDescriptionCompat {
         var duration = player?.duration
         return MediaDescriptionCompat.Builder()
             .setTitle(song.tittle)
             .setSubtitle(song.genre)
             .setDescription(index.toString())
             .setMediaId(song._id)
             .setExtras(bundleOf("DURATION" to duration))
             .setIconUri(Uri.parse(song.thumbnailPath?.replace("localhost" , "192.168.43.166")))
             .build()
     }
}

