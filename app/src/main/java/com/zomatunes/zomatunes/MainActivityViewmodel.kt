package com.zomatunes.zomatunes

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.data.db.AppDb
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.data.repo.DownloadRepository
import com.zomatunes.zomatunes.data.repo.RadioRepository
import com.zomatunes.zomatunes.data.repo.UserRepository
import com.zomatunes.zomatunes.media.ServiceConnector
import com.zomatunes.zomatunes.util.LibraryService
import com.zomatunes.zomatunes.util.NotificationHelper
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import javax.inject.Inject

class MainActivityViewmodel @ViewModelInject constructor(@Assisted var savedstate : SavedStateHandle, var serviceConnector: ServiceConnector, var libraryService : LibraryService,
                                                         var downloadTracker: DownloadTracker, var downloadRepo : DownloadRepository, var userRepo : UserRepository,
                                                         var radioRepo : RadioRepository, var appDb: AppDb) : ViewModel() {

    var url = "http://192.168.137.82"

    var playlistQueue  = MutableLiveData<List<Streamable<String,String>>>(emptyList())
    var audiobookPlaylistQueue = MutableLiveData<List<Chapter>>(emptyList())
    var playlistSelectedIndex = MutableLiveData(-1)

    var playedFrom = MutableLiveData("Now Playing")
    var showPlayerCard = true

    val songrecyclerviewStateManeger : RecyclerviewStateManager<Song<String, String>> by lazy {
        var stateManager = RecyclerviewStateManager<Song<String,String>>()
        stateManager
    }

    var isAudioPrepared = false
    var isAudiobookIntro = false
    var isLoadFromCache = false
    var isFavoriteSong = MutableLiveData(false)
    var isAlbumFavorite = MutableLiveData(false)


    var selectedRadio : Radio? = null

    // the following field used to track user listening when user moves between chapter and books
    var previousListeningBookChapterId : String? = null


    var error = userRepo.error

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
                                isShuffled : Boolean? = false , loadFromCache : Boolean = false , playerStateIndicator: Int? = 1){
        playlistQueue.value = songs

        previousListeningBookChapterId = null
        CoroutineScope(Dispatchers.IO).launch {

            if(loadFromCache) serviceConnector.initiatePlayer(songs , ServiceConnector.STREAM_TYPE_DOWNLOAD , isShuffled)
            else serviceConnector.initiatePlayer(songs , ServiceConnector.STREAM_TYPE_MEDIA , isShuffled , playerStateIndicator)
            playerState.postValue(state)
            if (position != null) play(position.toLong())
            else play()
        }
    }

     fun prepareAndPlayAudioBook(chapterList : List<Chapter> , isIntro : Boolean , state : PlayerState? , position: Int? , loadFromCache: Boolean = false ,
                                bookid : String? = null , chapterPosition : Long? = null , chapterIndex : Int? = null , duration: Int? = null) {


        playlistQueue.value = chapterList
        audiobookPlaylistQueue.value = chapterList
        playerState.value = state
        if(isIntro) serviceConnector.initiateAudioBookPlayer(chapterList , ServiceConnector.STREAM_TYPE_AUDIOBOOK_SAMPLE)
        else{
            if(loadFromCache){
                //store previously played audiobook info
                if(previousListeningBookChapterId != null){

                    var pos = playbackPosition.value
                    var dur = playbackDuration.value
                    Log.i("audioupdate" , "$pos $dur")
                    downloadRepo.updateChapterCurrentPosition(previousListeningBookChapterId!! , pos!!.toLong() , dur!!)
                }


                serviceConnector.initiateAudioBookPlayer(chapterList , ServiceConnector.STREAM_TYPE_AUDIOBOOK , bookid , chapterPosition , chapterIndex)
                playerState.value = state
                previousListeningBookChapterId = chapterList[position!!]._id
                if (position != null) play(position.toLong())
                else play()
            }
//            else serviceConnector.initiateAudioBookPlayer(chapterList , ServiceConnector.STREAM_TYPE_AUDIOBOOK)

        }
    }

    fun updateChapterCurrentPosition(bookId : String , chapterId : String , chapterIndex : Int , position : Long , duration : Int = 0){
        CoroutineScope(Dispatchers.IO).launch{

            bookId?.let {

                appDb.audiobookDao.updateLastListeningChapter(it , chapterIndex)  }
        }
    }

    fun continueSongStream(){
        serviceConnector.sendCommand(ServiceConnector.CONTINUE_SONG_STREAM)
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


   fun downloadSong(song : Song<String,String> , owner : LifecycleOwner ) = viewModelScope.launch{
       downloadRepo.downloadSongs(listOf(song))
       downloadTracker.addDownloads(listOf(song) , owner)
   }

    fun updateRadioStation(radioInfo : UserRadioInfo) = radioRepo.updateRadioStation(radioInfo)

    fun handleDownloadNotification(context : Context , lifecycleOwner: LifecycleOwner , downloadInfo: DbDownloadInfo) = viewModelScope.launch {
        withContext(Dispatchers.IO){
            downloadTracker.calculateProgress(
                downloadInfo, lifecycleOwner , null ,
                fun (progress : Int){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationHelper.buildAlbumDownloadNotification(context , downloadInfo._id!! , downloadInfo.name , progress)
                    }
                }){ NotificationHelper.buildCompleteNotification(context , downloadInfo._id!! , downloadInfo.name)
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


    fun updateFcmToken(token : String) = userRepo.updateFcmToken(token)

}
