package com.example.ethiomusic.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class MusicBrowseContent (
    var musicPlaylistHeader : String? = null,
    var songPlaylists : List<Playlist<String>>? = null,

    var newAlbumHeader : String? = null,
    var newAlbum : List<Album<String , String>>? = null,

    var popularAlbumHeader : String? = null,
    var popularAlbum : List<Album<String , String>>? = null,

    var playlistHeader : String? = null,
    var playlist: List<Playlist<String>>? = null,

    var artistHeader : String? = null,
    var artist: List<Artist<String , String>>? = null,

    var radioHeader : String? = null,
    var radioStations : List<Radio>? = null
)

@Parcelize
data class MusicBrowse(
    var name : String? = null ,
    var category : String? = null ,
    var contentType : String? = null,
    var imagePath : String? = null
) : Parcelable{

}