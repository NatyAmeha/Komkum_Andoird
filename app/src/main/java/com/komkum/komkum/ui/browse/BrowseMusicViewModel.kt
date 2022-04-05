package com.komkum.komkum.ui.browse

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komkum.komkum.IViewmodelFactory
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.*
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
        browseResult.value = result.data ?: null
    }

    fun getNewSongsPlaylist(genre : String? = null) = viewModelScope.launch{
        var result = playlistRepo.getNewSongPLaylists(genre)
        songPlaylists.value = result.data ?: null
    }

    fun getNewAlbums(genre : String? = null) = viewModelScope.launch {
        var result = albumRepo.getNewAlbums(genre)
        newAlbums.value = result.data ?: null
    }

    fun getPopularAlbums(genre : String? = null) = viewModelScope.launch {
        var result = albumRepo.getPopularAlbums(genre)
        popularAlbum.value = result.data ?: null
    }

    fun getTopArtist(genre: String? = null) = viewModelScope.launch{
        var result = artistRepo.getPopularArtistsSuspend()
        artistList.value = result.data ?: null
    }

    fun getRadio(genre: String?) = viewModelScope.launch{
        if(!genre.isNullOrEmpty()){
            var result = radioRepo.getRadioyGenre(genre)
            radioList.value = result.data ?: null
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