package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Parcelize
@Keep
data class LibraryInfo(
    var _id: String = "",
    @SerializedName(value = "songId" , alternate = ["albumId" , "artistId" , "authorId" , "podcastId" , "episodeId"])
    var vId : String? = null, // map songid , playlistid albumid artistid podcastid to _id
    var date: Date? = null,
    var type : Int? = null,
    var count : Int? = null,
    var name : String? = null
) : BaseModel(_id) , Parcelable{
    fun toBaseModel() = BaseModel(baseId = _id , baseTittle = name , baseSubTittle = count.toString())
    fun toLibraryInfo() = BaseModel(baseId = _id , baseTittle = name , baseSubTittle = count.toString() , baseType = type)

}

@Keep
@Parcelize
data class Library(
    var likedSong: List<LibraryInfo>? = null,
    var likedAlbums: List<LibraryInfo>? = null,
    var likedPlaylist: List<LibraryInfo>? = null,
    var likedPodcasts: List<LibraryInfo>? = null,
    var likedPodcastEpisods : List<LibraryInfo>? = null,
    var followedArtists: List<LibraryInfo>? = null,
    var favoriteAuthors : List<LibraryInfo>? = null,
    var radioStations : List<@RawValue UserRadioInfo>? = null
) : Parcelable{

    companion object{
        const val SONG_LIBRARY = 1
        const val ALBUM_LIBRARY = 2
        const val  ARTIST_LIBRARY = 3
        const val PLAYLIST_LIBRARY = 4
        const val PODCAST_LIBRARY = 5

    }

}

