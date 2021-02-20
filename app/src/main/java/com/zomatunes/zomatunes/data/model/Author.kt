package com.zomatunes.zomatunes.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

@Parcelize
data class Author<EBookType , AudiobookType>(
    var _id : String? = null,
    var name : String? = null,
    var description : String? = null,
    var genre : List<String>? = null,
    var books : List<@RawValue EBookType>? = null,
    var audiobooks : List<@RawValue AudiobookType>? = null,
    var profileImagePath : String? = null,
    var date : Date? = null,
    var followersId : List<String>
) : Parcelable

data class AuthorMetadata(
    var author : Author<Book<String> ,  Book<String>>? = null,
    var similarAuthor  : List<Author<String,String>>? = null,
    var salesNumber : Int? = null,
    var recommendedBooks : List<Author<String ,String>>? = null
)