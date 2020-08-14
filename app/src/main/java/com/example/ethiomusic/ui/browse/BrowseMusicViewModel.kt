package com.example.ethiomusic.ui.browse

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.data.repo.*
import com.example.ethiomusic.ui.song.SongViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class BrowseMusicViewModel(var savedStateHandle: SavedStateHandle , var songRepo : SongRepository, var playlistRepo : PlaylistRepository ,
                           var albumRepo : AlbumRepository , var artistRepo : ArtistRepository , var radioRepo : RadioRepository , var homeRepo : HomeRepository ) : ViewModel() {
    var browseResult = MutableLiveData<MusicBrowseContent>()
    var songPlaylists = MutableLiveData<List<Playlist<String>>>()
    var newAlbums = MutableLiveData<List<Album<String,String>>>()
    var popularAlbum = MutableLiveData<List<Album<String , String>>>()
    var artistList  = MutableLiveData<List<Artist<String,String>>>()
    var radioList = MutableLiveData<List<Radio>>()

    fun getBrowseResult(browseInfo : MusicBrowse) = viewModelScope.launch {
        var result = homeRepo.getBrowseResult(browseInfo)
        browseResult.value = result.data
    }

    fun getNewSongsPlaylist(genre : String? = null) = viewModelScope.launch{
        var result = playlistRepo.getNewSongPLaylists(genre)
        songPlaylists.value = result.data
    }

    fun getNewAlbums(genre : String? = null) = viewModelScope.launch {
        var result = albumRepo.getNewAlbum(genre)
        newAlbums.value = result.data
    }

    fun getPopularAlbums(genre : String? = null) = viewModelScope.launch {
        var result = albumRepo.getPopularAlbum(genre)
        popularAlbum.value = result.data
    }

    fun getTopArtist(genre: String? = null) = viewModelScope.launch{
        var result = artistRepo.getPopularArtistsSuspend()
        artistList.value = result.data
    }

    fun getRadio(genre: String?) = viewModelScope.launch{
        if(!genre.isNullOrEmpty()){
            var result = radioRepo.getRadioyGenre(genre)
            radioList.value = result.data
        }
    }


}

class BrowseMusicViewmodelFactory @Inject constructor(var songRepo : SongRepository, var playlistRepo : PlaylistRepository ,
                                                      var albumRepo : AlbumRepository  , var artistRepo : ArtistRepository ,
                                                      var radioRepo : RadioRepository ,  var homeRepo : HomeRepository  )
    : IViewmodelFactory<BrowseMusicViewModel> {

    override fun create(savedStateHandle: SavedStateHandle): BrowseMusicViewModel {
        return BrowseMusicViewModel(savedStateHandle , songRepo , playlistRepo , albumRepo , artistRepo , radioRepo , homeRepo)
    }

}