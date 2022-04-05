package com.komkum.komkum.ui.artist

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.IViewmodelFactory
import com.komkum.komkum.data.model.Artist
import com.komkum.komkum.data.model.ArtistMetaData
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.data.repo.ArtistRepository
import com.komkum.komkum.data.repo.DownloadRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtistViewModel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle, var artistRepo : ArtistRepository ,
var downloadRepo : DownloadRepository , var downloadTracker: DownloadTracker
) : ViewModel() {

    var error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String> {
        var a = MediatorLiveData<String>()
        a.addSource(artistRepo.error){ error.value = it }
        return a
    }

    fun removeOldErrors(){
        artistRepo.error.value = null
    }

    var artistsList = MutableLiveData<List<Artist<String , String>>>()
    suspend fun getArtistMetadata(artistId : String) = artistRepo.getArtistInfo(artistId)
    fun followArtists(artistIds : List<String>) = artistRepo.followArtist(artistIds)
    fun unfollowArtists(artistIds : List<String>) = artistRepo.unfollowArtists(artistIds)
    suspend fun isArtistInUserFavorite(artistId : String) = artistRepo.isArtistInFavorite(artistId).data

    fun getArtistByGenre(genre : List<String>){}
    fun getSearchResult(query : String) = artistRepo.getArtistSearchResult(query)

    fun downloadSongs(songs : List<Song<String, String>>) = viewModelScope.launch {
        downloadRepo.downloadSongs(songs)
    }

    var popularArtists = artistRepo.getPopularArtists()

    suspend fun getSimilarArtists(artistId: String) = artistRepo.getSimilarArtists(artistId)
    var newArtist = artistRepo.getNewArtists()

    var favoriteArtists = artistRepo.getUserFavoriteArtists()

    fun getUserFavoriteArtists() = viewModelScope.launch {
        artistsList.value = artistRepo.getUserFavoriteARtistsBeta().data ?: null
    }

}
