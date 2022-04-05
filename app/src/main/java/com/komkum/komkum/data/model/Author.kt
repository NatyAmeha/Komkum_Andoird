package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

import java.util.*

@Parcelize
@Keep
data class Author<EBookType , AudiobookType>(
    var _id : String? = null,
    var name : String? = null,
    var description : String? = null,
    var genre : List<String>? = null,
    var books : List<@RawValue EBookType>? = null,
    var audiobooks : List<@RawValue AudiobookType>? = null,
    var profileImagePath : String? = null,
    var date : Date? = null,
    var followersId : List<String>,
    var donationEnabled : Boolean? = true,
    var user : String? = null
) : Parcelable{
    companion object{
        const val FAVORITE_AUTHORS_LIST = 0
        const val PREV_AUTHORS_LIST = 1
    }
}
@Keep
data class AuthorMetadata(
    var author : Author<Book<String> ,  Book<String>>? = null,
    var similarAuthor  : List<Author<String,String>>? = null,
    var salesNumber : Int? = null,
    var recommendedBooks : List<Author<String ,String>>? = null
)