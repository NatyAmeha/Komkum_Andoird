package com.example.ethiomusic

import android.content.Context
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.data.db.AppDb
import com.example.ethiomusic.data.model.DbDownloadInfo
import com.example.ethiomusic.data.model.Radio
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.data.repo.DownloadRepository
import com.example.ethiomusic.data.repo.RadioRepository
import com.example.ethiomusic.media.ServiceConnector
import com.example.ethiomusic.util.LibraryService
import com.example.ethiomusic.util.NotificationHelper
import com.example.ethiomusic.util.viewhelper.PlayerState
import com.google.android.exoplayer2.offline.Download
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import javax.inject.Inject

class MainActivityViewmodel(var savedstate : SavedStateHandle , var serviceConnector: ServiceConnector , var libraryService : LibraryService ,
                            var downloadTracker: DownloadTracker , var downloadRepo : DownloadRepository ,var radioRepo : RadioRepository , var appDb: AppDb) : ViewModel() {

    var url = "http://10.0.2.2:4000/api/"

    var playlistQueue  = MutableLiveData<List<Song<String,String>>>(emptyList())
    var playlistSelectedIndex = MutableLiveData(-1)

    var isAudioPrepared = false
    var isFavoriteSong = MutableLiveData(false)
    var isAlbumFavorite = MutableLiveData(false)


    var selectedRadio : Radio? = null

    fun getSelectedSongInfo(songId : String) = playlistQueue.value?.find { song -> song._id == songId }
    fun getSelectedSongInfo(index : Int) = playlistQueue.value!![index]



    val playbackState = serviceConnector.playbackstate
    val nowPlaying = serviceConnector.nowPlaying
    var playerState = MutableLiveData<PlayerState>()
    var playbackDuration = serviceConnector.duration
    var playbackPosition = serviceConnector.currentPosition
    var queueitem = serviceConnector.queue

    fun connectToService() = serviceConnector.connect()
    fun disconnectToService() = serviceConnector.disconnect()
    fun preparePlayer(mediaType : Int , isShuffled : Boolean? = false) = serviceConnector.initiatePlayer(playlistQueue.value!! , mediaType , isShuffled)

    fun prepareAndPlaySongsBeta(songs : List<Song<String,String>>  , state: PlayerState ,  position: Int? ,
                                isShuffled : Boolean? = false , loadFromCache : Boolean = false){
        playlistQueue.value = songs
        if(loadFromCache) serviceConnector.initiatePlayer(playlistQueue.value!! , ServiceConnector.DOWNLOADED_MEDIA_TYPE , isShuffled)
        else serviceConnector.initiatePlayer(playlistQueue.value!! , ServiceConnector.STREAM_MEDIA_TYPE , isShuffled)
        playerState.value = state
        if (position != null) play(position.toLong())
        else play()
    }

    fun play() = serviceConnector.sendCommand(ServiceConnector.ACTION_PLAY)
    fun play(playlistIndex : Long) = serviceConnector.sendCommand(ServiceConnector.ACTION_SKIP_TO_ITEM , playlistIndex)
    fun pause() = serviceConnector.sendCommand(ServiceConnector.ACTION_PAUSE)
    fun next() = serviceConnector.sendCommand(ServiceConnector.ACTION_NEXT)
    fun prev() = serviceConnector.sendCommand(ServiceConnector.ACTION_PREV)
    fun rewind() = serviceConnector.sendCommand(ServiceConnector.ACTION_REWIND)
    fun forward() = serviceConnector.sendCommand(ServiceConnector.ACTION_FORWARD)

    fun seekTo(position : Long) = serviceConnector.sendCommand(ServiceConnector.ACTION_SEEK_TO , null , position = position)
    fun shuffle() = serviceConnector.sendCommand(ServiceConnector.ACTION_SHUFFLE)
    fun updateStreamCount() = serviceConnector.sendCommand(ServiceConnector.UPDATE_STREAM_COUNT)

    fun addToQueue(songList : MutableList<Song<String,String>>){
        if(playlistQueue.value!!.isEmpty()) playlistQueue.value = songList
        else {
            var initialList = playlistQueue.value!!.toMutableList()
            initialList.addAll(songList)
            playlistQueue.value  = initialList
        }
        serviceConnector.addToQueueList(songList)
    }

    fun removeQueueItem(position: Int) = serviceConnector.removeQueueItem(position)
    fun moveQueueItem(prevPosition : Int , newPosition : Int) = serviceConnector.moveQueueItem(prevPosition , newPosition)


   fun downloadSong(song : Song<String,String> ) = viewModelScope.launch{
       downloadRepo.downloadSongs(listOf(song))
   }

    fun updateRadioStation() = selectedRadio?.let { radioRepo.updateRadioStation(it) }

    fun handleDownloadNotification(context : Context , lifecycleOwner: LifecycleOwner , downloadInfo: DbDownloadInfo) = viewModelScope.launch {
        withContext(Dispatchers.IO){
            downloadTracker.calculateProgress(
                downloadInfo, lifecycleOwner , null ,
                fun (progress : Int){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationHelper().buildAlbumDownloadNotification(context , downloadInfo._id!! , downloadInfo.name , progress)
                    }
                }){ NotificationHelper().buildCompleteNotification(context , downloadInfo._id!! , downloadInfo.name)
            }
        }

    }

    fun isSongInFavorite(songId: String) = viewModelScope.launch { isFavoriteSong.value = libraryService.checkSongInFavorite(songId) }

    fun addToFavoriteList(type : Int , songIds : List<String>) = viewModelScope.launch {
        var addResult =   libraryService.addToFavorite(type , songIds)
        addResult?.let {
            when(type){
                LibraryService.CONTENT_TYPE_SONG -> isFavoriteSong.value = addResult
                LibraryService.CONTENT_TYPE_ALBUM -> isAlbumFavorite.value = addResult
            }
        }
    }
    fun removeFromFavorite(type : Int , ids : List<String>) = viewModelScope.launch {
        var removeResult =  libraryService.removeFromFavorite(type , ids)
        removeResult?.let {
            when(type){
                LibraryService.CONTENT_TYPE_SONG -> isFavoriteSong.value = !removeResult
                LibraryService.CONTENT_TYPE_ALBUM -> isAlbumFavorite.value = !removeResult
            }

        }
    }

    fun addToPlaylist(playlistId : String? , songIds : List<String>?) = libraryService.addToPlaylist(playlistId!! , songIds!!)
}

class MainActivityViewmodelFactory @Inject constructor(var serviceConnector: ServiceConnector , var libraryService : LibraryService,
                                                       var downloadTracker: DownloadTracker , var downloadRepo : DownloadRepository ,
                                                       var radioRepo : RadioRepository,var appDb: AppDb) : IViewmodelFactory<MainActivityViewmodel>{
    override fun create(savedStateHandle: SavedStateHandle): MainActivityViewmodel {
        return MainActivityViewmodel(savedStateHandle , serviceConnector , libraryService , downloadTracker , downloadRepo , radioRepo , appDb)
    }

}