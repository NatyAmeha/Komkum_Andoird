package com.example.ethiomusic.ui.album

import androidx.lifecycle.*
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.model.Album
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.data.repo.AlbumRepository
import com.example.ethiomusic.data.repo.DownloadRepository
import com.example.ethiomusic.data.repo.SongRepository
import com.example.ethiomusic.util.LibraryService
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlbumViewModel(var savedstate : SavedStateHandle , var albumRepo : AlbumRepository , var songRepo : SongRepository ,
                     var downloadRepo : DownloadRepository , var downloadTracker: DownloadTracker , var libraryService: LibraryService) : ViewModel() {

    val error = albumRepo.error
    var album  = MutableLiveData<Album<Song<String,String>,String>>()
    fun albumd(albumId : String ,   loadFromCache: Boolean) = albumRepo.getAlbum(albumId , loadFromCache)
    var albumSongs = mutableListOf<Song<String,String>>()

    val recyclerviewStateManager : RecyclerviewStateManager<Song<String,String>>  by lazy {
       var stateManager =  RecyclerviewStateManager<Song<String, String>>()
        stateManager._downloadingItems.value = downloadTracker.getCurrentlyDownloadedItems()
        stateManager

    }


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


class AlbumViewmodelFactory @Inject constructor(var albumRepo : AlbumRepository , var songRepo : SongRepository ,
                                                var downloadRepo : DownloadRepository ,  var downloadTracker: DownloadTracker , var libraryService: LibraryService) :
    IViewmodelFactory<AlbumViewModel> {
    override fun create(savedStateHandle: SavedStateHandle): AlbumViewModel {
        return AlbumViewModel(savedStateHandle , albumRepo , songRepo , downloadRepo , downloadTracker , libraryService)
    }
}
