package com.example.ethiomusic.data.model

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

data class HomeBeta(
    var recentActivity: List<RecentActivity>? = null,
    var newMusic: List<Playlist<String>>? = null,
    var madeForYou: List<Playlist<String>>? = null,
    var recommendedSongsPlaylist: List<Playlist<String>>? = null
)

