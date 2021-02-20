package com.zomatunes.zomatunes.ui.playlist

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.IViewmodelFactory
import com.zomatunes.zomatunes.data.model.Playlist
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.data.repo.DownloadRepository
import com.zomatunes.zomatunes.data.repo.PlaylistRepository
import com.zomatunes.zomatunes.data.repo.SongRepository
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaylistViewModel @ViewModelInject constructor(@Assisted var savedSTate : SavedStateHandle, var  playlistRepo : PlaylistRepository
                                                     , var songRepo : SongRepository, var downloadRepo : DownloadRepository, var downloadTracker: DownloadTracker) : ViewModel() {

    var error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(songRepo.error){ error.value = it }
        a.addSource(playlistRepo.error){ error.value = it}
        return a
    }

    var playlistData  = MutableLiveData<Playlist<Song<String,String>>>()
    var playlists = MutableLiveData<List<Playlist<String>>>()
    var playlistSongs = MutableLiveData<List<Song<String,String>>>()

    val recyclerviewStateManager : RecyclerviewStateManager<Song<String,String>> by lazy {
        var stateManager = RecyclerviewStateManager<Song<String,String>>()
        stateManager._downloadingItems.value = downloadTracker.getAllCurrentlyDownloadedItems()
        stateManager
    }

    var chartsList = playlistRepo.getChartsList()

    fun  recommendedSongByArtist() = viewModelScope.launch {
        playlistData.value =  playlistRepo.songRecommendationByArtist().data
    }

    fun createPlaylist(playlist : Playlist<String>) = playlistRepo.createPlaylist(playlist)

    fun getPlaylist(playlistId : String , loadFromCache : Boolean = false) = viewModelScope.launch {
        playlistData.value =  playlistRepo.getPlaylist(playlistId , loadFromCache).data
    }

    fun getUserPlaylist() = viewModelScope.launch {
        playlists.value =  playlistRepo.getUserPlaylist().data
    }



    fun getLikedSong() = viewModelScope.launch {
        var likedSongs  = songRepo.getLikedSongs()
        playlistData.value = likedSongs.data
    }

    suspend fun isPlaylistInLibrary(playlistId: String) = playlistRepo.isPlaylistInFavorite(playlistId).data

    suspend fun getPlaylistSongs(songIds : List<String>) = songRepo.getSongsList(songIds).data

    fun updatePlaylist(playlist: Playlist<String>) = playlistRepo.updatePlaylist(playlist)

    fun addPlaylisttoFav(playlistId : String) = playlistRepo.addPlaylistTofavorite(playlistId)

    fun removePlaylistSongs(playlist : Playlist<String>) = playlistRepo.removeSongFromPlaylist(playlist)

    fun downloadPlaylistSongs(songs : List<Song<String,String>>) = viewModelScope.launch {
        downloadRepo.downloadSongs(songs)
    }

    fun downloadPlaylist(playlist : Playlist<Song<String,String>>) = viewModelScope.launch{
        downloadRepo.downloadPlaylist(playlist)
    }

    fun makePlaylistPublic(playlistId: String) = liveData<Boolean> {
        var result = playlistRepo.makePlaylistPublic(playlistId).data
        emit(result ?: false)
    }

    fun deletePlaylist(playlistId : String) = playlistRepo.deletePlaylist(playlistId)

}
