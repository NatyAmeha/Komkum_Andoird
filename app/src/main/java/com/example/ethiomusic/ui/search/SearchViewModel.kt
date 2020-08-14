package com.example.ethiomusic.ui.search

import androidx.lifecycle.*
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.model.Album
import com.example.ethiomusic.data.model.Search
import com.example.ethiomusic.data.repo.AlbumRepository
import com.example.ethiomusic.data.repo.ArtistRepository
import com.example.ethiomusic.data.repo.HomeRepository
import com.example.ethiomusic.data.repo.SongRepository
import com.example.ethiomusic.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel(var savedState : SavedStateHandle , var homeeRepo : HomeRepository , var albumRepo : AlbumRepository ,
                      var songRepo : SongRepository , var artistRepo : ArtistRepository) : ViewModel() {
    var searchResult = MutableLiveData<Resource<Search>>()
    var error  = MutableLiveData<String>()

    fun getSearchResult(query : String , loadFromCache : Boolean = false) = viewModelScope.launch {
        var result = homeeRepo.getSearchResult(query , loadFromCache)
        searchResult.value = result
    }

    fun getSearchSuggestions() = viewModelScope.launch {
        var songResult = songRepo.getSongsBeta("popularsong")
        var artistResult = artistRepo.getPopularArtistsSuspend()
        var gospelAlbumResult = albumRepo.getPopularAlbumsBeta("populargospelalbum")
        var secularAlbumResult = albumRepo.getPopularAlbumsBeta("popularsecularalbum")
       secularAlbumResult.data?.let {
            gospelAlbumResult.data?.toMutableList()?.addAll(it)
        }
        var result = Search(songs = songResult.data , albums = secularAlbumResult.data?.shuffled() , artists = artistResult.data)
        searchResult.value = Resource.Success(result)
    }

    var musicBrowseString = songRepo.getMusicBrowsingString("ALL")
}


class SearchViewmodelFactory @Inject constructor( var homeRepo : HomeRepository , var albumRepo : AlbumRepository ,
                                                  var songRepo : SongRepository , var artistRepo : ArtistRepository) : IViewmodelFactory<SearchViewModel>{
    override fun create(savedStateHandle: SavedStateHandle): SearchViewModel {
        return SearchViewModel(savedStateHandle , homeRepo , albumRepo , songRepo , artistRepo)
    }

}
