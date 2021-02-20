package com.zomatunes.zomatunes.media

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.data.db.AppDb
import com.zomatunes.zomatunes.data.model.Chapter
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.data.model.Streamable
import com.zomatunes.zomatunes.data.repo.DownloadRepository
import com.zomatunes.zomatunes.data.repo.SongRepository
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class CustomPlaybackPreparer @Inject constructor(@ApplicationContext var context : Context, var player : ExoPlayer, var dataSourceFactory: DefaultDataSourceFactory,
                                                 var cacheDataSourceFactory : CacheDataSourceFactory, var downloadTracker: DownloadTracker ,
                                                 var songRepo : SongRepository, var appDb : AppDb , var downloadRepo: DownloadRepository) : MediaSessionConnector.PlaybackPreparer {

    lateinit var songPlaylist : ConcatenatingMediaSource
    var songPlaylistCurrentPosition = 0L
    var songPlaylistCurrentWindowIndex = 0
    var isSongPlaying = false
    var currentPlaylistInfo : MutableList<Streamable<String,String>> = mutableListOf()     // to save playlis info when there is a transition from song to audio audiobook sample

    var playerState : Int = -1

    lateinit var audioBookChaterPlaylist : ConcatenatingMediaSource

    var playerCoroutineScope = CoroutineScope(Dispatchers.IO)
    var playlistInfo : MutableList<Streamable<String,String>> = mutableListOf()
    var chapterPlaylistInfo : MutableList<Chapter> = mutableListOf()

    var mediaType : Int? = null
    var bookId : String? = null
    var currentChapterPosition : Long? = null
    var currentChapterIndex : Int? = null

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
        const val CONTINUE_SONG_STREAM = "10"
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
                    var songlist = it.getParcelableArrayList<Song<String,String>>("SONG_LIST")?.toList()
                    if (songlist != null) {
                        addToQueue(songlist)
                    }
                }
            }
            REMOVE_QUEUE_ITEM ->{
                extras?.let {
                    var index = it.getInt("POSITION")
                    songPlaylist.removeMediaSource(index)
                    playlistInfo.removeAt(index)
                }
            }
            MOVE_QUEUE_ITEM ->{
                extras?.let {
                    var currentIndex = it.getInt("PREV_POSITION")
                    var item = playlistInfo[currentIndex]
                    var newIndex = it.getInt("NEW_POSITION")

                    playlistInfo.removeAt(currentIndex)
                    playlistInfo.add(newIndex , item)
                    songPlaylist.moveMediaSource(currentIndex , newIndex)
                }
            }

            CONTINUE_SONG_STREAM -> continueSongStream()


        }
        return true
    }

    override fun getSupportedPrepareActions(): Long {
       return PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
               PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID
    }

    override fun onPrepareFromMediaId(mediaId: String?, playWhenReady: Boolean, extras: Bundle?) {
//       playerCoroutineScope.launch {
//           withContext(Dispatchers.Main){
//
//           }
//       }

        extras?.let { bundle ->
            bundle.getInt("MEDIA_TYPE").let {
                mediaType = it
            }
            val isShuffled = bundle.getBoolean("SHUFFLE")

            playerState = bundle.getInt("PLAYER_STATE")

            // if bundle containt song information
            bundle.getParcelableArrayList<Song<String,String>>("QUEUES")?.let {
                playlistInfo = it.toMutableList()
//                var songUri = playlistInfo.map { song -> song.mpdPath!! }
                var mpdPath = playlistInfo.map { song -> song.mpdPath!! }
                mediaType?.let {
                    var mediasources =
                        when (mediaType) {
                            ServiceConnector.STREAM_TYPE_MEDIA -> createStreamPlaylist(mpdPath)
                            else -> createDownloadPlaylist(playlistInfo)
                        }
                    songPlaylist = ConcatenatingMediaSource(*mediasources.toTypedArray())
                    player.shuffleModeEnabled = isShuffled
                    player.prepare(songPlaylist)
                }
            }

            // if the media type is audio audiobook
            bookId = bundle.getString("BOOK_ID")
            currentChapterPosition = bundle.getLong("CHAPTER_POSITION")
            currentChapterIndex = bundle.getInt("CHAPTER_INDEX")

            bundle.getParcelableArrayList<Chapter>("CHAPTER_QUEUE")?.let { it ->
                currentPlaylistInfo = playlistInfo
                playlistInfo = it.toMutableList()
                var chapterMpdPath = playlistInfo.map { chapter -> chapter.mpdPath!! }
                playlistInfo.map { chapeter -> chapeter._id!! }

                mediaType?.let {type ->
                    var mediaSource = when(type){
                        ServiceConnector.STREAM_TYPE_AUDIOBOOK_SAMPLE ->{
                            songPlaylistCurrentPosition = player.currentPosition
                            songPlaylistCurrentWindowIndex = player.currentWindowIndex
                            isSongPlaying = player.isPlaying
                            createStreamPlaylist(chapterMpdPath)
                        }
                        else ->{ createDownloadPlaylist(playlistInfo) }
                    }
                    audioBookChaterPlaylist = ConcatenatingMediaSource(*mediaSource.toTypedArray())

                    player.shuffleModeEnabled = false
                    player.prepare(audioBookChaterPlaylist)
                    player.seekTo(currentChapterIndex ?: 0 , currentChapterPosition ?: 0)
                    player.playWhenReady = true

                }
            }

        }

    }

    override fun onPrepareFromUri(uri: Uri?, playWhenReady: Boolean, extras: Bundle?) {
    }

    override fun onPrepare(playWhenReady: Boolean) {
    }



    fun createHlsSample(){
        var songUrl = "https://s3.amazonaws.com/storage.output.bucket/music/795b5de6-4a01-473e-8f68-f2c0dfb58bbb/index.m3u8"
        var uri = Uri.parse(songUrl)
        var hlsMediSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        player.prepare(hlsMediSource)
        player.playWhenReady = true
    }



     fun createDashMediaSource(mpdPath : String) : DashMediaSource{
        var songurl = mpdPath.replace("localhost" , AdapterDiffUtil.URL)
        var uri = Uri.parse(songurl)
        return DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

      fun createOfflineMediaSource(song: Streamable<String , String> ) : MediaSource?{
        var songurl = song.mpdPath?.replace("localhost" , AdapterDiffUtil.URL)
        var uri = Uri.parse(songurl)
        val qualityParams: DefaultTrackSelector.Parameters = DefaultTrackSelector().parameters
//        var downloadHelper = DownloadHelper.forDash(uri , dataSourceFactory , DefaultRenderersFactory(context) , drmSessionManager , qualityParams)
//        var request = downloadHelper.getDownloadRequest(song._id , null)

          var req =  downloadTracker.getDownloadRequest(song._id)
          return if(req != null) DownloadHelper.createMediaSource(req , cacheDataSourceFactory)
          else null

//        downloadTracker.getDownloads(Download.STATE_COMPLETED).observe(context. , androidx.lifecycle.Observer {  })
////        return DownloadHelper.createMediaSource(request , dataSourceFactory)
//        return  DashMediaSource.Factory(DefaultDashChunkSource.Factory(cacheDataSourceFactory) ,   cacheDataSourceFactory)
//            .createMediaSource(uri)
////        return  DashMediaSource.Factory(DefaultDashChunkSource.Factory(dataSourceFactory) , ).createMediaSource(uri)
    }


    fun createStreamPlaylist(mpdPath : List<String>) : List<DashMediaSource>{
       return  mpdPath.map { path -> createDashMediaSource(path) }
    }


    fun createDownloadPlaylist(songs : List<Streamable<String , String>>) : List<MediaSource>{
//        var sample = listOf(createDashMediaSource("http://localhost:4000/songs/d582b565df2120f48f8422dd/index.mpd"))
        return songs.map { song -> createOfflineMediaSource(song) }.filterNotNull()
    }


    fun addToQueue(songList : List<Song<String , String>>){
        var dashMediaList = songList.map { song -> createDashMediaSource(song.mpdPath!!) }
        if(!::songPlaylist.isInitialized){
            playlistInfo = songList.toMutableList()
            songPlaylist = ConcatenatingMediaSource(*dashMediaList.toTypedArray())
            player.prepare(songPlaylist)
            player.playWhenReady = true
        }
        else{
            playlistInfo.addAll(songList.toMutableList())
            songPlaylist.addMediaSources(dashMediaList)
        }
    }



    fun continueSongStream(){
        if(::songPlaylist.isInitialized and  !currentPlaylistInfo.isNullOrEmpty()){
            playlistInfo = currentPlaylistInfo
            player.prepare(songPlaylist)
            player.seekTo(songPlaylistCurrentWindowIndex , songPlaylistCurrentPosition)
            player.playWhenReady = isSongPlaying
        }
        else{
            player.stop()
        }
    }





    inner class PlayerListener : Player.EventListener {
        var streamTime = 0
        lateinit var timer : Timer
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
          when(playbackState){
              Player.STATE_ENDED ->  Log.i("playerstate" , "finished")
          }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if(mediaType == ServiceConnector.STREAM_TYPE_MEDIA){
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
                else{
                    if(::timer.isInitialized) timer.cancel()
                }
            }

//            else if(mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
//                if(!isPlaying){
//                    var duration = player.duration
//                    var currentPosition = player.currentPosition
//                    playerCoroutineScope.launch {
//                        var selectedChapterId = playlistInfo[player.currentWindowIndex]._id
//                        appDb.chapterDao.updateChapterCurrentPositionAndDuration(selectedChapterId , duration.toInt() )
//                        Log.i("chaptersel" ,  playlistInfo[player.currentWindowIndex].tittle+duration.toString())
//                    }
//                }
//
//            }

        }

        override fun onPositionDiscontinuity(reason: Int) {
            when(reason){
                Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ->{
                    streamTime = 0
                    Log.i("streamtimelinead" , "new timeline"+ player.currentPosition.toString())
                }
                Player.DISCONTINUITY_REASON_SEEK -> {
                    if(player.currentPosition.toInt() <= 0){
                        streamTime = 0
                        if(mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
                            downloadRepo.updateAudiobookCurrentChapter(bookId!! , player.currentWindowIndex)
                        }
                        Log.i("streamtimelineseeeeee" , "new timeline"+ player.currentPosition.toString())
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