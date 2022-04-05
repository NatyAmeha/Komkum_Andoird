package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Trivia(
    var _id : String? = null,
    var question : String? = null,
    var category :  String? = null,
    var choice : List<String>? = null,
    var answer : Int? = null,
    var time : Int? = null
) : Parcelable
