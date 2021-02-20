package com.zomatunes.zomatunes.ui.artist

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.IViewmodelFactory
import com.zomatunes.zomatunes.data.model.Artist
import com.zomatunes.zomatunes.data.model.ArtistMetaData
import com.zomatunes.zomatunes.data.repo.ArtistRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtistViewModel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle, var artistRepo : ArtistRepository) : ViewModel() {

    var error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String> {
        var a = MediatorLiveData<String>()
        a.addSource(artistRepo.error){ error.value = it }
        return a
    }
    var artistsList = MutableLiveData<List<Artist<String , String>>>()
    suspend fun getArtistMetadata(artistId : String) = artistRepo.getArtistInfo(artistId)
    fun followArtists(artistIds : List<String>) = artistRepo.followArtist(artistIds)
    fun unfollowArtists(artistIds : List<String>) = artistRepo.unfollowArtists(artistIds)
    suspend fun isArtistInUserFavorite(artistId : String) = artistRepo.isArtistInFavorite(artistId).data

    fun getArtistByGenre(genre : List<String>){}
    fun getSearchResult(query : String) = artistRepo.getArtistSearchResult(query)

    var popularArtists = artistRepo.getPopularArtists()
    var newArtist = artistRepo.getNewArtists()

    var favoriteArtists = artistRepo.getUserFavoriteArtists()

    fun getUserFavoriteArtists() = viewModelScope.launch {
        artistsList.value = artistRepo.getUserFavoriteARtistsBeta().data
    }

}
