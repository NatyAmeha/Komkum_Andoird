package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

import java.util.*

@Parcelize
@Keep
data class Artist<SongType , AlbumType>(
    var _id: String,
    var name: String,
    var description: String,
    var email: String,
    var dateCreated: Date,
    var category: String,
    var genre : List<String>? = null,
    var profileImagePath: List<String>? = null,

    var singleSongs: List<@RawValue SongType>,

    var albums: List<@RawValue AlbumType>,
    var followersId : List<String>? = null,
    var followersCount: Int,

    var totalStreamCount: Int? = null,
    var monthlyStreamCount : Int? = null,
    var totalDownloadCount : Int? = null,
    var monthlyDownloadCount : Int? = null,

    var paymentMethod: String,
    var donationEnabled : Boolean? = true,
    var user : String? = null
) : BaseModel(baseId = _id , baseTittle = name , baseSubTittle = category ){
    companion object{
        const val LOAD_FAVORITE_ARTIST = 0
        const val OTHER = 1
    }
    fun toBaseModel() =  BaseModel(baseId = _id , baseTittle = name , baseSubTittle = category )
}


@Parcelize
@Keep
data class ArtistMetaData(
   var artist  : Artist<Song<String , String> , Album<String , String>>,
   var topSongs : List<Song<String,String>>? = null,
   var totalSongs  : Int,
   var monthlyStream  : Int,
   var monthlyDownload  : Int,
   var totalStream  : Int,
   var totalDownload  : Int,
   var rank : Int

) : Parcelable