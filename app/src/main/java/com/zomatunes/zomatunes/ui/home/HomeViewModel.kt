package com.zomatunes.zomatunes.ui.home

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.IViewmodelFactory
import com.zomatunes.zomatunes.data.repo.*
import javax.inject.Inject

class  HomeViewModel @ViewModelInject constructor (@Assisted var savedstate : SavedStateHandle, var homeRepo : HomeRepository, var albumRepo : AlbumRepository,
                                                   var radioRepo : RadioRepository, var artistRepo : ArtistRepository, var playlistRepo : PlaylistRepository) : ViewModel() {

    val error = homeRepo.error
//    fun errorHandler() = MediatorLiveData<String>().addSource(homeRepo.error){errorResult -> error.value = errorResult }

    val homeData = homeRepo.getHomeData()

    val homeDataBeta = homeRepo.getHomeBeta()

    var popularArtists = artistRepo.getPopularArtists()
    var newArtists = artistRepo.getNewArtists()
    var recommendedAlbums = albumRepo.getRecommendedAlbums()

    var featureRadio = radioRepo.getFeaturedRadioStations()
//    var userRadioStation = radioRepo.getUserRadioStations()
    var popularRadio = radioRepo.getPopularRadioStations()

    var featurePlaylist = playlistRepo.getFeaturedPlaylist()

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
