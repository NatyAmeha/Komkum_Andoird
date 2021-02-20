package com.zomatunes.zomatunes.ui.album

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.data.model.Album
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.data.repo.AlbumRepository
import com.zomatunes.zomatunes.data.repo.DownloadRepository
import com.zomatunes.zomatunes.data.repo.SongRepository
import com.zomatunes.zomatunes.util.LibraryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumViewModel @ViewModelInject constructor(@Assisted var savedstate : SavedStateHandle, var albumRepo : AlbumRepository, var songRepo : SongRepository,
                                                  var downloadRepo : DownloadRepository, var downloadTracker: DownloadTracker, var libraryService: LibraryService) : ViewModel() {

    val error = albumRepo.error
    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(songRepo.error){ error.value = it }
        a.addSource(albumRepo.error){ error.value = it}
        return a
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
            album.value = result.data
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

