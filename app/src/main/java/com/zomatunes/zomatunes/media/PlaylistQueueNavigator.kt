package com.zomatunes.zomatunes.media

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.os.bundleOf
import com.zomatunes.zomatunes.data.model.Streamable
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
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

        player?.let {
            Log.i("tagplayer" ,playbackPreparer.playlistInfo[it.currentWindowIndex].tags.toString() + it.currentWindowIndex.toString())

            if(playbackPreparer.mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
                var chapterId = playbackPreparer.playlistInfo[it.currentWindowIndex]._id
                playbackPreparer.downloadRepo.updateChapterCurrentPosition(chapterId  , it.currentPosition , it.duration.toInt())
            }

            if(playbackPreparer.playerState == 3){
                    var index = it.currentWindowIndex
                    playbackPreparer.playlistInfo.removeAt(index)
                    playbackPreparer.songPlaylist.removeMediaSource(index)
            }
            it.next()
            it.playWhenReady = true
        }
    }


    override fun onSkipToPrevious(player: Player?, controlDispatcher: ControlDispatcher?) {
        player?.let {
            Log.i("tagplayer" ,playbackPreparer.playlistInfo[it.currentWindowIndex].tags.toString())

            if(playbackPreparer.mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
                var chapterId = playbackPreparer.playlistInfo[it.currentWindowIndex]._id
                playbackPreparer.downloadRepo.updateChapterCurrentPosition(chapterId  , it.currentPosition , it.duration.toInt())
            }
            // disable to prev funcationality when state is radio
            if(playbackPreparer.playerState != 3){
                it.previous()
                it.playWhenReady = true
            }
        }
    }

    override fun onSkipToQueueItem(player: Player?, controlDispatcher: ControlDispatcher?, id: Long) {
        player?.seekTo(id.toInt() , player.currentPosition)
        player?.playWhenReady = true
        if(playbackPreparer.mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
            playbackPreparer.downloadRepo.updateAudiobookCurrentChapter(playbackPreparer.bookId!! , id.toInt())
        }

    }

     fun getMediaDescription(streamable : Streamable<String,String>, index : Int, player: Player?) : MediaDescriptionCompat {
         var duration = player?.duration
         return MediaDescriptionCompat.Builder()
             .setTitle(streamable.tittle)
             .setSubtitle(streamable.genre)
             .setDescription(index.toString())
             .setMediaId(streamable._id)
             .setExtras(bundleOf("DURATION" to duration , "LYRICS" to streamable.lyrics , "PLAYBACK_STATE" to playbackPreparer.playerState))
             .setIconUri(Uri.parse(streamable.thumbnailPath?.replace("localhost" , AdapterDiffUtil.URL)))
             .build()
     }
}

