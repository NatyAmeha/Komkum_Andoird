package com.example.ethiomusic.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@Parcelize
data class LibraryInfo(
    var _id: String = "",
    @SerializedName(value = "songId" , alternate = ["albumId" , "artistId"])
    var vId : String? = null, // map songid , playlistid albumid artistid podcastid to _id
    var date: Date? = null,
    var type : Int? = null,
    var count : Int? = null,
    var name : String? = null
) : BaseModel(_id){
    fun toBaseModel() = BaseModel(baseId = _id , baseTittle = name , baseSubTittle = count.toString())
    fun toLibraryInfo() = BaseModel(baseId = _id , baseTittle = name , baseSubTittle = count.toString() , baseType = type)

}

data class Library(
    var likedSong: Array<LibraryInfo>? = null,
    var likedAlbums: Array<LibraryInfo>? = null,
    var likedPlaylist: Array<LibraryInfo>? = null,
    var likedPodcasts: Array<LibraryInfo>? = null,
    var followedArtists: Array<LibraryInfo>? = null
){

    companion object{
        const val SONG_LIBRARY = 1
        const val ALBUM_LIBRARY = 2
        const val  ARTIST_LIBRARY = 3
        const val PLAYLIST_LIBRARY = 4
        const val PODCAST_LIBRARY = 5

    }
//    suspend fun getLibrarySummery() : List<LibraryInfo>{
//       return  withContext(Dispatchers.Default){
//            var sortedSong =  likedSong?.sortedBy { song -> song.date }
//            var sortedAlbuum = likedAlbums?.sortedBy { album -> album.date }
//            var sortedArtist = followedArtists?.sortedBy { artist -> artist.date }
//
//           var librarySummery = mutableListOf<LibraryInfo>()
//           if(!sortedSong.isNullOrEmpty())   librarySummery.add( LibraryInfo(sortedSong[0]._id , sortedSong[0].vId , sortedSong[0].date , SONG_LIBRARY , sortedSong.size , "Liked Songs"))
//           if(!sortedAlbuum.isNullOrEmpty())   librarySummery.add( LibraryInfo(sortedAlbuum[0]._id , sortedAlbuum[0].vId , sortedAlbuum[0].date , ALBUM_LIBRARY , sortedAlbuum.size , "Favorite Albums"))
//           if(!sortedArtist.isNullOrEmpty())  librarySummery.add(LibraryInfo(sortedArtist[0]._id , sortedArtist[0].vId , sortedArtist[0].date , ARTIST_LIBRARY , sortedArtist.size , "Favorite Artists"))
//            return@withContext librarySummery
//        }
//
//
//    }
}

