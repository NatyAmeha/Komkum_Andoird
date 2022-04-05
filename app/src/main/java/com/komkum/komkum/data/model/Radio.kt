package com.komkum.komkum.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

import java.util.*

@Parcelize
@Keep
data class Radio(
    var _id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var creatorName: String? = null,
    var stationType: Int? = null,
    var radioQueryTag: @RawValue RadioQueryTag? = null,
    var featured: Boolean? = null,
    var songs: List<Song<String, String>>? = null,
    var coverImage: String? = null,
    var baseSongId: String? = null,
    var baseArtistId: String? = null,
    var listenersId: List<String>? = null
) : Parcelable {
    companion object {
        const val STATION_TYPE_SONG = 3
        const val STATION_TYPE_ARTIST = 4
    }
}

@Parcelize
@Keep
data class RadioQueryTag(
    var genres: List<String>? = null, var time: String? = null,
    var tags: List<String>? = null, var category: String? = null,
    var date: @RawValue DateQuery? = null
) : Parcelable

@Keep
@Parcelize
data class UserRadioInfo(
    var radioId: String,
    var likedTag: List<String>? = null,
    var unlikedTag: List<String>? = null
) : Parcelable

@Keep
@Parcelize
data class RadioLikeInfo(
    var radioName: String,
    var radioType: Int,
    var baseId: String? = null   // this can be artist, song id or null depends on radiotype
) : Parcelable