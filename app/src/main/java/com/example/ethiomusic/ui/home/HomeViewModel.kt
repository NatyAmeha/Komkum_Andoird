package com.example.ethiomusic.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.repo.*
import javax.inject.Inject

class  HomeViewModel @Inject constructor (var savedstate : SavedStateHandle , var homeRepo : HomeRepository , var albumRepo : AlbumRepository,
                                          var radioRepo : RadioRepository , var artistRepo : ArtistRepository , var playlistRepo : PlaylistRepository) : ViewModel() {

    val error = homeRepo.error

    val homeData = homeRepo.getHomeData()

    val homeDataBeta = homeRepo.getHomeBeta()

    var popularArtists = artistRepo.getPopularArtists()
    var newArtists = artistRepo.getNewArtists()
    var recommendedAlbums = albumRepo.getRecommendedAlbums()

    var featureRadio = radioRepo.getFeaturedRadioStations()
    var userRadioStation = radioRepo.getUserRadioStations()
    var popularRadio = radioRepo.getPopularRadioStations()

    var featurePlaylist = playlistRepo.getFeaturedPlaylist()

//    val recommendationByTag = homeRepo.getRecommendationTags()
//
//    val recommendationByArtist = homeRepo.songRecommendationByArtist()

}

class HomeViewmodelFactory @Inject constructor(var  homeRepo : HomeRepository , var albumRepo : AlbumRepository,  var radioRepo : RadioRepository ,
                                               var artistRepo : ArtistRepository ,  var playlistRepo : PlaylistRepository) : IViewmodelFactory<HomeViewModel>{
    override fun create(savedStateHandle: SavedStateHandle): HomeViewModel {
        return HomeViewModel(savedStateHandle , homeRepo , albumRepo , radioRepo , artistRepo , playlistRepo)
    }
}
