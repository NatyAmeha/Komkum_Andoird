package com.example.ethiomusic.data.model

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
    var listenersId : List<String>? = null
) : Parcelable


data class RadioQueryTag(var genres : List<String> , var time : String , var tags : List<String>)
