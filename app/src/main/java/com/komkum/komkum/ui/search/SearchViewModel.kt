package com.komkum.komkum.ui.search

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.Search
import com.komkum.komkum.data.repo.AlbumRepository
import com.komkum.komkum.data.repo.ArtistRepository
import com.komkum.komkum.data.repo.HomeRepository
import com.komkum.komkum.data.repo.SongRepository
import kotlinx.coroutines.launch

class SearchViewModel @ViewModelInject constructor(@Assisted var savedState : SavedStateHandle, var homeeRepo : HomeRepository, var albumRepo : AlbumRepository,
                                                   var songRepo : SongRepository, var artistRepo : ArtistRepository) : ViewModel() {
    var error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(songRepo.error){ error.value = it }
        a.addSource(artistRepo.error){ error.value = it}
        a.addSource(homeeRepo.error){ error.value = it}
        a.addSource(albumRepo.error){ error.value = it}
        return a
    }

    fun removeOldErrors(){
        songRepo.error.value = null
        artistRepo.error.value = null
        homeeRepo.error.value = null
        albumRepo.error.value = null

    }
    var searchResult = MutableLiveData<Search>()
    var musicBrowseList = MutableLiveData<List<MusicBrowse>>()


    fun getSearchResult(query : String , loadFromCache : Boolean = false) = viewModelScope.launch {
        var result = homeeRepo.getSearchResult(query , loadFromCache)
        searchResult.value = result.data ?: null
    }

    fun getSearchSuggestions() = viewModelScope.launch {
        var recommendation = homeeRepo.getSearchRecommendation().data
        getBrowseListItem()
        searchResult.value = recommendation ?: null
    }

    fun getBrowseListItem() = viewModelScope.launch{
        var result =  songRepo.getBrowseCategories("ALL").data
        musicBrowseList.value = result ?: null
    }


}

