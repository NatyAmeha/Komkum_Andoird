package com.zomatunes.zomatunes.data.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

@Parcelize
data class Radio(
    var _id : String? = null,
    var name : String? = null,
    var description  : String? = null,
    var creatorName  : String? = null,
    var stationType  : Number? = null,
    var radioQueryTag : @RawValue RadioQueryTag,
    var featured : Boolean,
    var songs : List<Song<String , String>>,
    var coverImage : String? = null,
    var baseSongId : String? = null,
    var baseArtistId : String? = null,
    var listenersId : List<String>? = null
) : Parcelable{
    companion object{
        const val STATION_TYPE_SONG = 6
        const val STATION_TYPE_ARTIST = 7
    }
}

@Parcelize
data class RadioQueryTag(var genres : List<String>? = null , var time : String? = null ,
                         var tags : List<String>? = null  , var category : String? = null ,
                         var date : @RawValue DateQuery? = null) : Parcelable

data class UserRadioInfo(
    var radioId : String,
    var likedTag : List<String>? = null,
    var unlikedTag : List<String>? = null
)