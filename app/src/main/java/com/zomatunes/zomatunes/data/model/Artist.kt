package com.zomatunes.zomatunes.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

@Parcelize
data class Artist<SongType , AlbumType>(
    var _id: String,
    var name: String,
    var description: String,
    var email: String,
    var dateCreated: Date,
    var category: String,
    var genre : List<String>? = null,
    var profileImagePath: List<String>,

    var singleSongs: List<@RawValue SongType>,

    var albums: List<@RawValue AlbumType>,
//
    var followersId : List<String>? = null,
    var followersCount: Int,
    var paymentMethod: String
) : BaseModel(baseId = _id , baseTittle = name , baseSubTittle = category ){
    companion object{
        const val LOAD_FAVORITE_ARTIST = 0
        const val OTHER = 1
    }
    fun toBaseModel() =  BaseModel(baseId = _id , baseTittle = name , baseSubTittle = category )
}


@Parcelize
data class ArtistMetaData(
   var artist  : Artist<Song<String , String> , Album<Song<String , String> , String>>,
   var totalSongs  : Int,
   var monthlyStream  : Int,
   var monthlyDownload  : Int,
   var totalStream  : Int,
   var totalDownload  : Int

) : Parcelable