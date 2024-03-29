package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

import org.jetbrains.annotations.NotNull
import java.util.*

@Parcelize
@Keep
open  class Book<T>(
    var _id : String? = null,
    var name : String? = null,
    var description : String? = null,
    var genre : List<String>? = null,
    var length : String? = null,
    var releaseDate : Date? = null,
    var language : String? = null,
    var format : String? = null,
    var coverImagePath : String? = null,
    var author : List<@RawValue T>? = null,
    var authorName : List<String>? = null,
    var publisher : String? = null,
    var credit : List<String>? = null,
    var rating : List<@RawValue Review>? = null,

    var priceInBirr : Float? = null,
    var priceInDollar : Float? = null,

    var promotion : Boolean? = null,
    var exclusive : Boolean? = null,
    var downloadCount : Int? = null
) : Parcelable{
    companion object{
        const val AudiobookFormat = "AUDIOBOOK"
        const val EbookFormat = "EBOOK"
    }
}

@Parcelize
@Keep
data class EBook<AuthorType>(
    var sample : String? = null,      // file location
    var bookFile : String? = null,     // file loaction
    var audiobook : String? = null
) : Book<AuthorType>() ,  Parcelable

@Entity
data class EbookDbInfo(
    @PrimaryKey
    @NotNull
    var _id : String,
    var name : String,
    var coverImagePath : String,
    var authorName : String,
    var lastReadingPage : Int = 0
)