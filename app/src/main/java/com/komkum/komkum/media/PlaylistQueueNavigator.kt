package com.komkum.komkum.media

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.os.bundleOf
import com.komkum.komkum.data.model.Streamable
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.komkum.komkum.util.notification.FcmService


class PlaylistQueueNavigator(mediasession : MediaSessionCompat , var playbackPreparer: CustomPlaybackPreparer) : TimelineQueueNavigator(mediasession) {
    val firebaseAnalytics = Firebase.analytics

    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        Log.i("tagplayer" , "description $windowIndex")
        return getMediaDescription(playbackPreparer.playlistInfo[windowIndex] , windowIndex , player)
    }

    override fun getSupportedQueueNavigatorActions(player: Player): Long {
        return  PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
    }


    override fun onSkipToNext(player: Player, controlDispatcher: ControlDispatcher) {
        Log.i("tagplayer" , "next")
        player?.let {
//            Log.i("tagplayer" ,playbackPreparer.playlistInfo[it.currentWindowIndex].tags.toString() + it.currentWindowIndex.toString())

            var selectedStream = playbackPreparer.playlistInfo[it.currentWindowIndex]
            if(playbackPreparer.mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
                var chapterId = selectedStream._id
                playbackPreparer.downloadRepo.updateChapterCurrentPositionAndDuration(chapterId  , it.currentPosition , it.duration.div(1000).toInt())
            }


            it.next()
            it.playWhenReady = true
        }
    }


    override fun onSkipToPrevious(player: Player, controlDispatcher: ControlDispatcher) {
        Log.i("tagplayer" , "previous")
        player?.let {
            Log.i("tagplayer" , it.currentTimeline.isEmpty.toString()+"  " + player.currentWindowIndex)

            var selectedStream = playbackPreparer.playlistInfo[it.currentWindowIndex]

            if(playbackPreparer.mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
                var chapterId = selectedStream._id
                playbackPreparer.downloadRepo.updateChapterCurrentPositionAndDuration(chapterId  , it.currentPosition , it.duration.div(1000).toInt())
            }


            // disable to prev funcationality when state is radio
            it.previous()
            it.playWhenReady = true
        }
    }

    override fun onSkipToQueueItem(player: Player, controlDispatcher: ControlDispatcher, id: Long) {
        Log.i("tagplayer" , "queue item")
        if(playbackPreparer.mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK)
            player.seekTo(id.toInt() , player.currentPosition)
        else player.seekTo(id.toInt() , 0)
        player.playWhenReady = true
        if(playbackPreparer.mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
            playbackPreparer.downloadRepo.updateAudiobookCurrentChapter(playbackPreparer.bookId!! , id.toInt())
        }
    }


     fun getMediaDescription(streamable : Streamable<String,String>, index : Int, player: Player?) : MediaDescriptionCompat {
         var duration = player?.duration
         var trackType = when(playbackPreparer.playerState){
             1 -> FcmService.F_EVENT_PARAM_VALUE_MUSIC
             2 -> FcmService.F_EVENT_PARAM_VALUE_AUDIOBOOK
             3 -> FcmService.F_EVENT_PARAM_VALUE_MUSIC
             4 -> FcmService.F_EVENT_PARAM_VALUE_PODCAST
             else -> FcmService.F_EVENT_PARAM_VALUE_MUSIC
         }
         firebaseAnalytics.logEvent(FcmService.F_EVENT_NEW_TRACK){
             param(FcmService.F_EVENT_PARAM_TRACK_TYPE , trackType)
         }
         return MediaDescriptionCompat.Builder()
             .setTitle(streamable.tittle ?: "")
             .setSubtitle(streamable.ownerName?.joinToString(", ") ?: "Artist Names")
             .setDescription(index.toString() ?: "")
             .setMediaId(streamable._id)
             .setExtras(bundleOf("DURATION" to duration , "LYRICS" to streamable.lyrics ,
                 "PLAYBACK_STATE" to playbackPreparer.playerState , "IS_AD" to streamable.isAd ,
                 "AD_TYPE" to streamable.adType , "AD_CONTENT" to streamable.adContent))
             .setIconUri(Uri.parse(streamable.thumbnailPath))
             .build()
     }
}

