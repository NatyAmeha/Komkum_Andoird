package com.komkum.komkum.data.model

import androidx.annotation.Keep

@Keep
data class Home(
    var newSong: List<Song<String, String>>? = null,
    var popularSong: List<Song<String, String>>? = null,
    var newAlbum: List<Album<String, String>>? = null,
    var popularGospelAlbum: List<Album<String, String>>? = null,
    var popularSecularAlbum: List<Album<String, String>>? = null,
    var popularOrtodoxAlbum: List<Album<String, String>>? = null,
    var popularIslamicAlbum: List<Album<String, String>>? = null,
    var recentActivity: List<RecentActivity>? = null,
    var newPodcast: List<Any>? = null,
    var populartPodcast: List<Any>? = null
)

@Keep
data class HomeBeta(
    var recentActivity: List<RecentActivity>? = null,
    var newMusic: List<Playlist<String>>? = null,
    var madeForYou: List<Playlist<String>>? = null,
    var topCharts: List<Playlist<String>>? = null,
    var popularArtist : List<Artist<String,String>>? = null,
    var newArtist: List<Artist<String,String>>? = null,
    var newAlbum : List<Album<String,String>>? = null,
    var recommendedAlbum : List<Album<String,String>>? = null,
    var featuredPlaylist : List<Playlist<String>>? = null,
    var featuredRadio : List<Radio>? = null,
    var popularRadio : List<Radio>? = null,
    var topCreatorByDonation : List<DonationLeaderBoard>? = null
)

