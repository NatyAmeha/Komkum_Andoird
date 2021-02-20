package com.zomatunes.zomatunes.ui.search

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.data.model.MusicBrowse
import com.zomatunes.zomatunes.data.model.Search
import com.zomatunes.zomatunes.data.repo.AlbumRepository
import com.zomatunes.zomatunes.data.repo.ArtistRepository
import com.zomatunes.zomatunes.data.repo.HomeRepository
import com.zomatunes.zomatunes.data.repo.SongRepository
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
    var searchResult = MutableLiveData<Search>()
    var musicBrowseList = MutableLiveData<List<MusicBrowse>>()


    fun getSearchResult(query : String , loadFromCache : Boolean = false) = viewModelScope.launch {
        var result = homeeRepo.getSearchResult(query , loadFromCache)
        searchResult.value = result.data
    }

    fun getSearchSuggestions() = viewModelScope.launch {
        var recommendation = homeeRepo.getSearchRecommendation().data
        getBrowseListItem()
        searchResult.value = recommendation
    }

    fun getBrowseListItem() = viewModelScope.launch{
        var result =  songRepo.getBrowseCategories("ALL").data
        musicBrowseList.value = result
    }


}

