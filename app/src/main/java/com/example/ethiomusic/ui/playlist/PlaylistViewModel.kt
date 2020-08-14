package com.example.ethiomusic.ui.playlist

import androidx.lifecycle.*
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.data.repo.DownloadRepository
import com.example.ethiomusic.data.repo.PlaylistRepository
import com.example.ethiomusic.data.repo.SongRepository
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaylistViewModel(var savedSTate : SavedStateHandle , var  playlistRepo : PlaylistRepository
                        , var songRepo : SongRepository , var downloadRepo : DownloadRepository , var downloadTracker: DownloadTracker) : ViewModel() {

    var playlistData  = MutableLiveData<Playlist<Song<String,String>>>()
    var playlistSongs = MutableLiveData<List<Song<String,String>>>()

    val recyclerviewStateManager : RecyclerviewStateManager<Song<String,String>> by lazy {
        var stateManager = RecyclerviewStateManager<Song<String,String>>()
        stateManager._downloadingItems.value = downloadTracker.getCurrentlyDownloadedItems()
        stateManager}

    fun  recommendedSongByArtist() = viewModelScope.launch {
        playlistData.value =  playlistRepo.songRecommendationByArtist().data
    }
    fun getPlaylist(playlistId : String , loadFromCache : Boolean = false) = viewModelScope.launch {
        playlistData.value =  playlistRepo.getPlaylist(playlistId , loadFromCache).data
    }

    fun getPlaylistSongs(songIds : List<String>) = songRepo.getSongsList(songIds)

    fun updatePlaylist(playlist: Playlist<String>) = playlistRepo.updatePlaylist(playlist)

    fun removePlaylistSongs(playlist : Playlist<String>) = playlistRepo.removeSongFromPlaylist(playlist)

    fun downloadPlaylistSongs(songs : List<Song<String,String>>) = viewModelScope.launch {
        downloadRepo.downloadSongs(songs)
    }

    fun downloadPlaylist(playlist : Playlist<Song<String,String>>) = viewModelScope.launch{
        downloadRepo.downloadPlaylist(playlist)
    }

    fun deletePlaylist(playlistId : String) = playlistRepo.deletePlaylist(playlistId)

}


class PlaylistViewModelFactory @Inject constructor(var  playlistRepo : PlaylistRepository , var songRepo : SongRepository ,
                                                   var downloadRepo : DownloadRepository , var downloadTracker: DownloadTracker ) : IViewmodelFactory<PlaylistViewModel> {
    override fun create(savedStateHandle: SavedStateHandle): PlaylistViewModel {
        return PlaylistViewModel(savedStateHandle , playlistRepo , songRepo , downloadRepo , downloadTracker)
    }
}