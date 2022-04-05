package com.komkum.komkum.media

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.Chapter
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.data.model.Streamable
import com.komkum.komkum.data.repo.DownloadRepository
import com.komkum.komkum.data.repo.SongRepository
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.komkum.komkum.data.model.PodcastEpisode
import com.komkum.komkum.data.repo.AdRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class CustomPlaybackPreparer @Inject constructor(@ApplicationContext var context : Context , var player : ExoPlayer, var dataSourceFactory: DefaultDataSourceFactory,
                                                 var cacheDataSourceFactory : CacheDataSourceFactory, var downloadTracker: DownloadTracker ,
                                                 var songRepo : SongRepository , var adRepo : AdRepository, var appDb : AppDb , var downloadRepo: DownloadRepository) : MediaSessionConnector.PlaybackPreparer {

    lateinit var songPlaylist : ConcatenatingMediaSource
    var songPlaylistCurrentPosition = 0L
    var songPlaylistCurrentWindowIndex = 0
    var isSongPlaying = false
    var currentPlaylistInfo : MutableList<Streamable<String,String>> = mutableListOf()     // to save playlis info when there is a transition from song to audio audiobook sample

    var playerState : Int = -1
    var streamTime = 0
    var isStreamCounterActive = true
    lateinit var timer : Timer

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

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {

    }


    override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle?, cb: ResultReceiver?): Boolean {
        Log.i("command" , command)

        when(command){
            REPEAT_ON -> {
             player.repeatMode = Player.REPEAT_MODE_ONE
            }
            SHUFFLE ->{
                player.shuffleModeEnabled = true
                player.playWhenReady = true
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

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        extras?.let { bundle ->
            bundle.getInt("MEDIA_TYPE").let {
                mediaType = it
            }
            val isShuffled = bundle.getBoolean("SHUFFLE")

            playerState = bundle.getInt("PLAYER_STATE")
            var songPosition = bundle.getInt("POSITION" , -1)
            // if bundle containt song information
            bundle.getParcelableArrayList<Song<String,String>>("QUEUES")?.let {
                playlistInfo = it.toMutableList()
//                var songUri = playlistInfo.map { song -> song.mpdPath!! }
                var mpdPath = playlistInfo.map { song -> song.mpdPath!! }

                mediaType?.let {
                    var mediasources = when (mediaType) {
                            ServiceConnector.STREAM_TYPE_MEDIA ->{
//                                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
//                                    Log.i("adaptivestreamtype" , "hls")
//                                    createProgressiveStreamPlaylist(mp3Path)
//                                }
//                                else{
//                                    Log.i("adaptivestreamtype" , "progressive")
//                                    createDashStreamPlaylist(mpdPath)
//                                }
                                createDashStreamPlaylist(playlistInfo)
                            }
                            else -> createOfflinePlaylist(playlistInfo)
                        }
                    songPlaylist = ConcatenatingMediaSource(*mediasources.toTypedArray())

//                    var mediaItem = createAdsMediaSource()

                    player.shuffleModeEnabled = isShuffled
//                    player.addMediaItem(mediaItem)
                    player.setMediaSource(songPlaylist)
                    player.prepare()
                    player.playWhenReady = true
                    if(songPosition != -1) player.seekTo(songPosition , 0)
                    else player.playWhenReady = true
                }
            }


            bundle.getParcelableArrayList<PodcastEpisode>("EPISODE_QUEUE")?.let {
                playlistInfo = it.toMutableList()
                mediaType?.let {
                    var filePath = playlistInfo.map { playlist -> playlist.songFilePath!! }
                    var mediasources = when (mediaType) {
                        ServiceConnector.STREAM_TYPE_PODCAST_EPISODE ->{
                            createProgressiveStreamPlaylist(filePath)
                        }
                        else -> createProgressiveOfflinePlaylist(filePath)
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
                            createDashStreamPlaylist(playlistInfo)
                        }
                        else ->{ createOfflinePlaylist(playlistInfo) }
                    }
                    audioBookChaterPlaylist = ConcatenatingMediaSource(*mediaSource.toTypedArray())

                    player.shuffleModeEnabled = false
                    player.prepare(audioBookChaterPlaylist)
                    Log.i("current postion" , currentChapterPosition.toString())
                    player.seekTo(currentChapterIndex ?: 0 , currentChapterPosition ?: 0)
                    player.playWhenReady = true

                }
            }
        }

    }

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
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



     fun createDashMediaSource(mpdPath : String) : HlsMediaSource{
//         https://s3.amazonaws.com/storage.output.bucket/music/00799f7e-87a0-42a5-a33a-bb406c6b57df/index.m3u8
//         https://s3-eu-west-1.amazonaws.com/storage.output.bucket.er/music/08dc4fce-e0fa-4a88-b557-2d8d501d6db5/index.m3u8
//        var songurl = mpdPath.replace("storage.output.bucket" , "storage.output.bucket.er")
//            .replace("s3.amazonaws.com" , "s3-eu-west-1.amazonaws.com")
        var uri = Uri.parse(mpdPath)
        return HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(uri)
    }

    fun createAdsMediaSource(path : List<String>) : MediaSource{
        var uri = Uri.parse(path.first())
        return HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(uri)
    }

     fun createProgressiveMediaSource(path : String) : ProgressiveMediaSource{
         var url = Uri.parse(path)
         return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(url)
     }

      fun createOfflineMediaSource(song: Streamable<String , String> , isAdaptive : Boolean = true ) : MediaSource?{
        var songurl = if(isAdaptive) song.mpdPath?.replace("localhost" , AdapterDiffUtil.URL)
           else song.songFilePath?.replace("localhost" , AdapterDiffUtil.URL)

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

    fun createProgressiveOffline(path : String) : ProgressiveMediaSource{
        var url = Uri.parse(path)
        return ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(url)
    }


    fun createDashStreamPlaylist(playlists :MutableList< Streamable<String , String>>) : List<MediaSource>{
        return playlists.map { streamInfo ->
            if(streamInfo.isAd == true){
                createProgressiveMediaSource(streamInfo.mpdPath!!)
            }
            else createDashMediaSource(streamInfo.mpdPath!!)
        }
    }


    fun createProgressiveStreamPlaylist(url : List<String>) : List<ProgressiveMediaSource>{
        return url.map { path -> createProgressiveMediaSource(path) }
    }

    fun createProgressiveOfflinePlaylist(url : List<String>) : List<ProgressiveMediaSource>{
        return url.map { path -> createProgressiveOffline(path) }
    }


    fun createOfflinePlaylist(songs : List<Streamable<String , String>> , isAdaptive: Boolean = true) : List<MediaSource>{
//        var sample = listOf(createDashMediaSource("http://localhost:4000/songs/d582b565df2120f48f8422dd/index.mpd"))
        return songs.map { song -> createOfflineMediaSource(song , isAdaptive) }.filterNotNull()
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


//    fun setupStreamCountUpdete(){
//        if(mediaType == ServiceConnector.STREAM_TYPE_MEDIA && selectedStream.isAd == false){
//            if(isPlaying){
//
//                preference
//
//                timer =  fixedRateTimer("main" , false , 0 , 1000){
//                    if(player.isPlaying){
//                        var duration = player.duration.toInt()
//                        var currentIndex = player.currentWindowIndex
//                        streamTime += 1
////                        if(streamTime > (duration/2000)){
////                            Log.i("streamtimeline" , "first canceled")
//////                            cancel()
////                            return@fixedRateTimer
////                        }
//                        if(streamTime >= (duration/2000) && isStreamCounterActive){
//                            streamTime = 0
//                            isStreamCounterActive = false
//
//                            playerCoroutineScope.launch {
//                                var song = playlistInfo[currentIndex]
//                                songRepo.updateSongStreamCount(song._id).data?.let{
//                                    withContext(Dispatchers.Main){
//                                        if(it){
//                                            Toast.makeText(context , "stream count updated" , Toast.LENGTH_LONG).show()
////                                            this@fixedRateTimer.cancel()
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//                    if(!isStreamCounterActive){
//
//                    }
//
//
//
//                }
//            }
//            else{
//                if(::timer.isInitialized) timer.cancel()
//            }
//        }
//    }


    inner class PlayerListener : Player.Listener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.i("playererrorstate" , playbackState.toString() + "  ${player.currentPosition}   ${playlistInfo.size}")
            when(playbackState){
                Player.STATE_ENDED ->  Log.i("playerstate" , "finished")
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
//            Toast.makeText(context , error?.message , Toast.LENGTH_LONG).show()

            if(error.type  == ExoPlaybackException.TYPE_SOURCE){
                if(mediaType == ServiceConnector.STREAM_TYPE_DOWNLOAD &&
                    (player.currentWindowIndex < playlistInfo.size -1 || player.currentWindowIndex < chapterPlaylistInfo.size -1)){
                    Log.i("playererror" , error?.type.toString() + "  "+ error?.message)
                    player.next()
                    player.retry()
                    Toast.makeText(context , "Skip files not downloaded completely" , Toast.LENGTH_LONG).show()
                }
                else player.retry()
            }
            else player.retry()

        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            var selectedStream = playlistInfo[player.currentWindowIndex]
            if(mediaType == ServiceConnector.STREAM_TYPE_MEDIA && selectedStream.isAd == false){
                if(isPlaying){
                    var duration = player.duration.toInt()
                    var currentIndex = player.currentWindowIndex
                    timer =  fixedRateTimer("main" , false , 0 , 1000){
                        streamTime += 1

                        if(streamTime > (duration/2000)){
                            Log.i("streamtimeline" , "first canceled")
                            cancel()
                            return@fixedRateTimer
                        }
                        if(streamTime == (duration/2000)){

                            playerCoroutineScope.launch {
                                withContext(Dispatchers.Main){
                                    var song = playlistInfo[currentIndex]
                                    songRepo.updateSongStreamCount(song._id).data?.let{
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

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            super.onPlaybackParametersChanged(playbackParameters)
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            Log.i("ontimeline" ,  reason.toString()+"     "+player.currentWindowIndex.toString())

        }


        override fun onLoadingChanged(isLoading: Boolean) {}

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            player.playWhenReady = false
            player.playWhenReady = true
            when(reason){
                Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ->{    //when we click next or prev
                    streamTime = 0
//                    downloadRepo.updateAudiobookCurrentChapter(bookId!! , id.toInt())
                    Log.i("streamtimelinead" , "new timeline"+ player.currentWindowIndex.toString())
                }
                Player.DISCONTINUITY_REASON_SEEK -> {   //when move to another song

                    if(player.currentPosition.toInt() <= 0){
                        streamTime = 0
                        if(mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
                            downloadRepo.updateChapterState(playlistInfo[player.currentWindowIndex]._id , false)
                            downloadRepo.updateAudiobookCurrentChapter(bookId!! , player.currentWindowIndex)
                        }
                        Log.i("streamtimelineseeeeee" , "new timeline"+ player.currentPosition.toString())
                    }
                }
                else ->{
                    streamTime = 0
                    if(mediaType == ServiceConnector.STREAM_TYPE_AUDIOBOOK){
                        var chapterId = playlistInfo[player.currentWindowIndex-1]._id

                        downloadRepo.updateChapterState(chapterId , true)
                        downloadRepo.updateAudiobookCurrentChapter(bookId!! , player.currentWindowIndex)
                    }

                    var selectedStream = playlistInfo[player.currentWindowIndex]
                    if(selectedStream.isAd == true){
                        playerCoroutineScope.launch {
                            var result = adRepo.updateAdsImpression(selectedStream._id).data
                            Log.i("ad update" , result.toString())
                        }
                    }
                    Log.i("streamtimelinetran" , "new timeline $reason   ${player.currentWindowIndex}")
//                    if(playlistInfo[player.currentWindowIndex].isAd == false){
//                        var prevAdIndex = player.currentWindowIndex -1
//                        songPlaylist.removeMediaSource(prevAdIndex)
//                        playlistInfo.removeAt(prevAdIndex)
//
//                    }
                }

            }
        }

    }

}