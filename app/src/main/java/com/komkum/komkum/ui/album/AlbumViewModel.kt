package com.komkum.komkum.ui.album

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.data.model.Album
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.data.repo.AlbumRepository
import com.komkum.komkum.data.repo.DownloadRepository
import com.komkum.komkum.data.repo.SongRepository
import com.komkum.komkum.util.LibraryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumViewModel @ViewModelInject constructor(@Assisted var savedstate : SavedStateHandle, var albumRepo : AlbumRepository, var songRepo : SongRepository,
                                                  var downloadRepo : DownloadRepository, var downloadTracker: DownloadTracker, var libraryService: LibraryService) : ViewModel() {

    val error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(songRepo.error){ error.value = it }
        a.addSource(libraryService.songRepo.error){error.value = it}
        a.addSource(libraryService.albumRepo.error){error.value = it}
        a.addSource(albumRepo.error){ error.value = it}
        return a
    }

    fun removeOldErrors(){
        songRepo.error.value = null
        libraryService.error.value = null
        albumRepo.error.value = null
    }

    var album  = MutableLiveData<Album<Song<String,String>,String>>()
    fun albumd(albumId : String ,   loadFromCache: Boolean) = albumRepo.getAlbum(albumId , loadFromCache)
    var albumSongs = mutableListOf<Song<String,String>>()

//    val recyclerviewStateManager : RecyclerviewStateManager<Song<String,String>>  by lazy {
//       var stateManager =  RecyclerviewStateManager<Song<String, String>>()
//        stateManager._downloadingItems.value = downloadTracker.getAllCurrentlyDownloadedItems()
//        stateManager
//    }


    fun getalbumB(albumId: String , loadFromCache : Boolean = false) = viewModelScope.launch{
        var result = albumRepo.getAlbumBeta(albumId , loadFromCache)
        result.data?.let {
            album.value = result.data ?: null
            albumSongs = result.data!!.songs!!.toMutableList()
        }
    }

    fun downloadAlbum(album : Album<Song<String,String> , String>) = viewModelScope.launch {
        downloadRepo.downloadAlbum(album)
    }

    fun addToFav(albumId: String) = liveData {
        var result = libraryService.addToFavorite(LibraryService.CONTENT_TYPE_ALBUM , listOf(albumId))
        emit(result)
    }

    fun removeFromFav(albumId: String) = liveData {
        var result = libraryService.removeFromFavorite(LibraryService.CONTENT_TYPE_ALBUM , listOf(albumId))
        emit(result!!)
    }

     fun getdownload(albumId: String) = viewModelScope.launch{
        withContext(Dispatchers.IO){
            downloadRepo.appDb.downloadDao.getDownload(albumId)
        }

    }

    fun downloadAlbumSongs(songs : List<Song<String,String>>) = viewModelScope.launch {
        downloadRepo.downloadSongs(songs)
    }

    suspend fun isAlbumDownloaded(contentId : String) : Boolean{
        return withContext(Dispatchers.IO){
            return@withContext downloadRepo.isInDownload(contentId)
        }
    }
//
//    fun addSongsToFavorite(songIds : List<String>) = songRepo.likeSong(songIds)
//
//    fun addToFavoriteAlbums(albumIds : List<String>) = albumRepo.addTofavorite(albumIds)
    suspend fun isAlbumInfavorite(albumId : String) = albumRepo.isAlbumInfavorite(albumId)


}

