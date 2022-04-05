package com.komkum.komkum.ui.home

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.data.repo.*
import com.komkum.komkum.data.viewmodel.ProductHomepageViewmodel
import kotlinx.coroutines.launch

class  HomeViewModel @ViewModelInject constructor (@Assisted var savedstate : SavedStateHandle, var homeRepo : HomeRepository, var albumRepo : AlbumRepository,
                                                   var radioRepo : RadioRepository, var artistRepo : ArtistRepository,
                                                   var playlistRepo : PlaylistRepository , var adRepo : AdRepository) : ViewModel() {

    val error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String>{
        var errorSource = MediatorLiveData<String>()
        errorSource.addSource(albumRepo.error){ error.value = it}
        errorSource.addSource(homeRepo.error){error.value = it}
        errorSource.addSource(radioRepo.error){error.value = it}
        errorSource.addSource(artistRepo.error){error.value = it}
        errorSource.addSource(adRepo.error){error.value = it}
        errorSource.addSource(playlistRepo.error){error.value = it}

        return errorSource
    }

    fun removeOldErrors(){
        albumRepo.error.value = null
        homeRepo.error.value = null
        radioRepo.error.value = null
        artistRepo.error.value = null
        adRepo.error.value = null
        playlistRepo.error.value = null

    }

    val homeData = homeRepo.getHomeData()

    var storeHomepageResult = MutableLiveData<ProductHomepageViewmodel>()

    val homeDataBeta = homeRepo.getHomeBeta()
    val productHomepageResult = homeRepo.getProductHomepage()


    var popularArtists = artistRepo.getPopularArtists()
    var newArtists = artistRepo.getNewArtists()
    var recommendedAlbums = albumRepo.getRecommendedAlbums()

    var featureRadio = radioRepo.getFeaturedRadioStations()
//    var userRadioStation = radioRepo.getUserRadioStations()
    var popularRadio = radioRepo.getPopularRadioStations()

    var featurePlaylist = playlistRepo.getFeaturedPlaylist()


    fun getStoreHomepageResult(long : Double? = null , lat : Double? = null) = viewModelScope.launch {
        var result = homeRepo.getProductHomepageBeta(long , lat).data
        if(result != null) storeHomepageResult.value = result!!
    }

    fun updateProductAdClick(adId : String) = liveData {
        var result = adRepo.updateAdClick(adId)
        emit(result)
    }

//    val recommendationByTag = homeRepo.getRecommendationTags()
//
//    val recommendationByArtist = homeRepo.songRecommendationByArtist()

}

//class HomeViewmodelFactory @Inject constructor(var  homeRepo : HomeRepository , var albumRepo : AlbumRepository,  var radioRepo : RadioRepository ,
//                                               var artistRepo : ArtistRepository ,  var playlistRepo : PlaylistRepository) : IViewmodelFactory<HomeViewModel>{
//    override fun create(savedStateHandle: SavedStateHandle): HomeViewModel {
//        return HomeViewModel(savedStateHandle , homeRepo , albumRepo , radioRepo , artistRepo , playlistRepo)
//    }
//}
