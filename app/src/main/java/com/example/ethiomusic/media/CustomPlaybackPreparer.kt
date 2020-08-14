package com.example.ethiomusic.media

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.data.repo.SongRepository
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import okhttp3.MediaType
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class CustomPlaybackPreparer @Inject constructor(var context : Context, var player : ExoPlayer , var dataSourceFactory: DefaultDataSourceFactory ,
                                                 var cacheDataSourceFactory : CacheDataSourceFactory ,  var songRepo : SongRepository) : MediaSessionConnector.PlaybackPreparer {

    lateinit var playlist : ConcatenatingMediaSource

    var playerCoroutineScope = CoroutineScope(Dispatchers.IO)
    var playlistInfo : MutableList<Song<String,String>> = mutableListOf()
    var mediaType : Int? = null

    companion object{
        var ADD_SINGLE_SONG_TO_PLAYLIST = "0"
        var ADD_MULTIPLE_SONG_TO_PLAYLIST = "1"
        var SHUFFLE = "SHUFFLE"
        var SHUFFLE_OFF = "3"
        var REPEAT_ON = "4"
        var REPEAT_OFF = "5"
        var UPDATE_STREAM_COUNT = "6"
        var ADD_TO_QUEUE = "7"
        var REMOVE_QUEUE_ITEM = "8"
        var MOVE_QUEUE_ITEM = "9"
    }

    init {
        player.addListener(PlayerListener())
    }

    override fun onPrepareFromSearch(query: String?, playWhenReady: Boolean, extras: Bundle?) {
    }

    override fun onCommand(player: Player?, controlDispatcher: ControlDispatcher?, command: String?, extras: Bundle?, cb: ResultReceiver?): Boolean {
        when(command){
            REPEAT_ON -> {
             player?.repeatMode = Player.REPEAT_MODE_ONE
            }
            SHUFFLE ->{
                player?.shuffleModeEnabled = true
                player?.playWhenReady = true
            }
            UPDATE_STREAM_COUNT ->{ }
            ADD_TO_QUEUE ->{
                extras?.let {
                    var songlist = it.getParcelableArrayList<Song<String,String>>("SONG_LIST")
                    addToQueue(songlist)
                }
            }
            REMOVE_QUEUE_ITEM ->{
                extras?.let {
                    var index = it.getInt("POSITION")
                    playlistInfo.removeAt(index)
                    playlist.removeMediaSource(index)
                }
            }
            MOVE_QUEUE_ITEM ->{
                extras?.let {
                    var currentIndex = it.getInt("PREV_POSITION")
                    var item = playlistInfo[currentIndex]
                    var newIndex = it.getInt("NEW_POSITION")

                    playlistInfo.removeAt(currentIndex)
                    playlistInfo.add(newIndex , item)
                    playlist.moveMediaSource(currentIndex , newIndex)
                }
            }
        }
        return true
    }

    override fun getSupportedPrepareActions(): Long {
       return PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
               PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID
    }

    override fun onPrepareFromMediaId(mediaId: String?, playWhenReady: Boolean, extras: Bundle?) {
        extras?.let { bundle ->
            bundle.getInt("MEDIA_TYPE").let { mediaType = it }
            var isShuffled = bundle.getBoolean("SHUFFLE")

            bundle.getParcelableArrayList<Song<String,String>>("QUEUES")?.let {
                playlistInfo = it
                var songUri = playlistInfo.map { song -> song.songFilePath!! }
                var mpdPath = playlistInfo.map { song -> song.mpdPath!! }
                mediaType?.let {
                    var mediasources =
                        if(mediaType == ServiceConnector.STREAM_MEDIA_TYPE) dashPlaylist(mpdPath)
                        else downloadPlaylist(songUri)
                    playlist = ConcatenatingMediaSource(*mediasources.toTypedArray())
                    player.shuffleModeEnabled = isShuffled
                    player.prepare(playlist)
                }
            }

        }
    }

    override fun onPrepareFromUri(uri: Uri?, playWhenReady: Boolean, extras: Bundle?) {
    }

    override fun onPrepare(playWhenReady: Boolean) {
    }



    fun createMediasource(songFilePath : String) : ProgressiveMediaSource{
        var songurl = songFilePath.replace("localhost" , "192.168.43.166")
        var uri = Uri.parse(songurl)
        return  ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }


    fun dashMediaSource(mpdPath : String) : DashMediaSource{
        var songurl = mpdPath.replace("localhost" , "192.168.43.166")
        var uri = Uri.parse(songurl)
        return DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    fun createDownloadMediaSource(songFilePath: String ) : ProgressiveMediaSource{
        var songurl = songFilePath.replace("localhost" , "192.168.43.166")
        var uri = Uri.parse(songurl)
        return  ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(uri)
    }

    fun dashPlaylist(songPathList : List<String>) : List<DashMediaSource>{
       return  songPathList.map { songPath -> dashMediaSource(songPath) }
    }
    fun downloadPlaylist(songPathList : List<String>) : List<ProgressiveMediaSource>{
        var sample = listOf(dashMediaSource("http://localhost:4000/songs/d582b565df2120f48f8422dd/index.mpd"))
         return songPathList.map { songPath -> createDownloadMediaSource(songPath) }
    }

    fun addtoPlaylist(songPathList : List<String> , mediaType: Int) : List<ProgressiveMediaSource>{
        var sample = listOf(dashMediaSource("http://localhost:4000/songs/d582b565df2120f48f8422dd/index.mpd"))
        return if(mediaType == ServiceConnector.STREAM_MEDIA_TYPE) songPathList.map { songPath -> createMediasource(songPath) }
        else songPathList.map { songPath -> createDownloadMediaSource(songPath) }
    }


    fun addToQueue(songList : List<Song<String , String>>){
        var dashMediaList = songList.map { song -> dashMediaSource(song.mpdPath!!) }
        if(!::playlist.isInitialized){
            playlistInfo = songList.toMutableList()
            playlist = ConcatenatingMediaSource(*dashMediaList.toTypedArray())
            player.prepare(playlist)
            player.playWhenReady = true
        }
        else{
            playlistInfo.addAll(songList.toMutableList())
            playlist.addMediaSources(dashMediaList)
        }
    }

    inner class PlayerListener : Player.EventListener {
        var streamTime = 0
        lateinit var timer : Timer
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if(mediaType == ServiceConnector.STREAM_MEDIA_TYPE){
                if(isPlaying){
                    Log.i("streamtimelinetran" , "transition")
                    timer =  fixedRateTimer(null , false , 0 , 1000){
                        streamTime += 1
                        if(streamTime > (player.duration.toInt()/2000)){
                            Log.i("streamtimeline" , "first canceled")
                            cancel()
                            return@fixedRateTimer
                        }
                        Log.i("streamtimeline" , streamTime.toString() +"  " +(player.duration/2000).toString())
                        if(streamTime == (player.duration.toInt()/2000)){

                            playerCoroutineScope.launch {
                                var song = playlistInfo[player.currentWindowIndex]
                                songRepo.updateSongStreamCount(song._id).data?.let{
                                    withContext(Dispatchers.Main){
                                        if(it){
                                            Toast.makeText(context , "stream count updated" , Toast.LENGTH_LONG).show()
                                            this@fixedRateTimer.cancel()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else timer?.let { it.cancel() }
            }

        }

        override fun onPositionDiscontinuity(reason: Int) {
            when(reason){
                Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ->{
                    streamTime = 0
                    Log.i("streamtimelinead" , "new timeline")
                }
                Player.DISCONTINUITY_REASON_SEEK -> {
                    if(player.currentPosition.toInt() <= 0){
                        streamTime = 0
                        Log.i("streamtimelinesee" , "new timeline")
                    }
                }
                else ->{
                    streamTime = 0
                    Log.i("streamtimelinetran" , "new timeline")
                }

            }

        }
    }

}