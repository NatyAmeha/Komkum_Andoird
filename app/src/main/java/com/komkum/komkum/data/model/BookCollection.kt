package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Keep
data class BookCollection(
    val _id : String? = null,
    var name : String? = null,
    var description : String? = null,
    var genre : List<String>? = null,
    var featured : Boolean = false,
    var curator : String? = null,
    var date: Date? = null,
    var books : List<Book<String>>? = null,
    var favorite : List<String>
) : Parcelable