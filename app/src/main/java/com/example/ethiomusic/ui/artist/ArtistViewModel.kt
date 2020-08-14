package com.example.ethiomusic.ui.artist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.model.ArtistMetaData
import com.example.ethiomusic.data.repo.ArtistRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtistViewModel(var savedStateHandle: SavedStateHandle , var artistRepo : ArtistRepository) : ViewModel() {


    fun  getArtistMetadata(artistId : String) = artistRepo.getArtistInfo(artistId)
    fun followArtists(artistIds : List<String>) = artistRepo.followArtist(artistIds)
    fun unfollowArtists(artistIds : List<String>) = artistRepo.unfollowArtists(artistIds)
    suspend fun isArtistInUserFavorite(artistId : String) = artistRepo.isArtistInFavorite(artistId)

    fun getArtistByGenre(genre : List<String>){}
    fun getSearchResult(query : String) = artistRepo.getArtistSearchResult(query)

    var popularArtists = artistRepo.getPopularArtists()
    var newArtist = artistRepo.getNewArtists()
    var favoriteArtists = artistRepo.getUserFavoriteArtists()
}

class ArtistViewmodelFactory @Inject constructor(var artistRepo : ArtistRepository) :
    IViewmodelFactory<ArtistViewModel> {
    override fun create(savedStateHandle: SavedStateHandle): ArtistViewModel {
        return ArtistViewModel(savedStateHandle , artistRepo)
    }
}