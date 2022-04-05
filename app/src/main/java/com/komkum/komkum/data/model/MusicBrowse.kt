package com.komkum.komkum.data.model

import android.os.Parcelable
import android.view.textclassifier.TextLanguage
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Keep
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
@Keep
data class BrowseQueryInfo(var tag : List<String>? = null , var genre : String? = null ,
                           var language: String? = null ,
                           var date : @RawValue DateQuery? = null) : Parcelable{


}
@Parcelize
@Keep
data class DateQuery(var startDate : Date , var endDate : Date) : Parcelable

@Parcelize
@Keep
data class MusicBrowse(
    var name : String? = null ,
    var category : String? = null ,
    var contentType : String? = null,
    var imagePath : String? = null,
    var queryInfo : @RawValue BrowseQueryInfo? = null

) : Parcelable{
 companion object{
     const val CONTENTY_TYPE_MUSIC = "MUSIC"
     const val CONTENT_TYPE_BOOK = "BOOK"
     const val CONTENT_TYPE_PODCAST = "PODCAST"
 }
}