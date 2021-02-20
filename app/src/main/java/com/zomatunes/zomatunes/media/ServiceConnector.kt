package com.zomatunes.zomatunes.media

import android.content.ComponentName
import android.content.Context
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.os.ResultReceiver
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.zomatunes.zomatunes.data.model.Chapter
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.data.model.Streamable
import com.zomatunes.zomatunes.util.extensions.exhaustive
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.google.android.exoplayer2.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.FieldPosition
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


class ServiceConnector @Inject constructor(@ApplicationContext  var context : Context, var player : ExoPlayer) {
    lateinit var mediaBrowser : MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat

    var playbackstate = MutableLiveData<PlaybackStateCompat>()
    var nowPlaying = MutableLiveData<MediaMetadataCompat>()
    var queue = MutableLiveData<List<MediaSessionCompat.QueueItem>>()
    var currentPosition  = MutableLiveData(0)
    var duration  = MutableLiveData(0)


    companion object{
        const val  ACTION_PLAY = 1
        const val  ACTION_PAUSE = 2
         const val  ACTION_NEXT = 3
        const val  ACTION_PREV = 4
        const val  ACTION_STOP = 5
        const val ACTION_SKIP_TO_ITEM = 6
        const val ACTION_SEEK_TO = 7
        const val ACTION_SHUFFLE = 8
        const val UPDATE_STREAM_COUNT = 9
        const val UPDATE_DOWNLOAD_COUNT= 10

        const val STREAM_TYPE_DOWNLOAD = 11
        const val STREAM_TYPE_MEDIA = 12
        const val STREAM_TYPE_AUDIOBOOK = 13
        const val STREAM_TYPE_AUDIOBOOK_SAMPLE = 14
        const val ACTION_FORWARD = 15
        const val ACTION_REWIND = 16

        const val REMOVE_QUEUE_ITEM = 17
        const val MOVE_QUEUE_ITEM = 18

        const val CONTINUE_SONG_STREAM = 19
    }



    fun connect(){
        mediaBrowser = MediaBrowserCompat(context , ComponentName(context , PlaybackService::class.java) , object : MediaBrowserCompat.ConnectionCallback(){
            override fun onConnected() {
                mediaController = MediaControllerCompat(context  , mediaBrowser.sessionToken)
                queue.value =  mediaController.queue
                playbackstate.value = mediaController.playbackState
                nowPlaying.value = mediaController.metadata

                registerControllerCallback()
            }

        } , null)
        mediaBrowser.connect()
    }

    fun disconnect() = mediaBrowser.disconnect()

    fun initiatePlayer(songlist : List<Streamable<String,String>> , mediaType : Int , shuffled : Boolean? = false , playerState : Int? = 1){
        if(mediaBrowser.isConnected){
//            Log.i("tagplayer" , songlist.map { song -> song. })
            var bundle = bundleOf("QUEUES" to songlist , "MEDIA_TYPE" to mediaType , "SHUFFLE" to shuffled , "PLAYER_STATE" to playerState)
            mediaController.transportControls.prepareFromMediaId("STREAM" , bundle)
        }
    }

    fun initiateAudioBookPlayer( chapterList : List<Chapter> , mediaType: Int , bookId : String? = null , currentPosition : Long? = null ,  currentIndex : Int? = null){
        if(mediaBrowser.isConnected){
            var bundle = bundleOf("BOOK_ID" to bookId , "CHAPTER_QUEUE" to chapterList , "MEDIA_TYPE" to mediaType ,  "PLAYER_STATE" to 2,
                "CHAPTER_POSITION" to currentPosition , "CHAPTER_INDEX" to currentIndex)
            mediaController.transportControls.prepareFromMediaId("STREAM" , bundle)
        }
    }

    fun sendCommand(action : Int , index : Long? = null , newIndex : Int? = null , position: Long? = null){
        when(action){
            ACTION_PLAY -> mediaController.transportControls.play()
            ACTION_PAUSE -> mediaController.transportControls.pause()
            ACTION_NEXT -> mediaController.transportControls.skipToNext()
            ACTION_PREV -> mediaController.transportControls.skipToPrevious()
            ACTION_REWIND -> mediaController.transportControls.rewind()
            ACTION_FORWARD -> mediaController.transportControls.fastForward()
            ACTION_SKIP_TO_ITEM -> mediaController.transportControls.skipToQueueItem(index!!)
            ACTION_SEEK_TO -> mediaController.transportControls.seekTo(position!!)

            ACTION_SHUFFLE -> mediaController.sendCommand(CustomPlaybackPreparer.SHUFFLE , null , null)
            UPDATE_STREAM_COUNT -> mediaController.sendCommand(CustomPlaybackPreparer.UPDATE_STREAM_COUNT , null , null)
            CONTINUE_SONG_STREAM -> mediaController.sendCommand(CustomPlaybackPreparer.CONTINUE_SONG_STREAM , null , null)

        }
    }
    fun addToQueueList(songlist: List<Song<String, String>>){
        var bundle = bundleOf("SONG_LIST" to songlist)
        mediaController.sendCommand(CustomPlaybackPreparer.ADD_TO_QUEUE , bundle , null)
    }

    fun removeQueueItem(position : Int){
        var bundle = bundleOf("POSITION" to position )
        mediaController.sendCommand(CustomPlaybackPreparer.REMOVE_QUEUE_ITEM , bundle , null)
    }

    fun moveQueueItem(prevPosition : Int , newPosition : Int){
        var bundle = bundleOf("PREV_POSITION" to prevPosition , "NEW_POSITION" to newPosition)
        mediaController.sendCommand(CustomPlaybackPreparer.MOVE_QUEUE_ITEM , bundle , null)
    }

    fun registerControllerCallback(){
        CoroutineScope(Dispatchers.IO).launch {
            fixedRateTimer(null , false , 0 , 1000){
                currentPosition.postValue(mediaController.playbackState.position.toInt())
                duration.postValue(player.duration.toInt())
            }
        }
        mediaController.registerCallback(object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
               playbackstate.value = state
            }
            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
               nowPlaying.value = metadata
            }

        })
    }
}