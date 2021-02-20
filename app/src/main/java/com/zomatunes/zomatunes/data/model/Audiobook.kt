package com.zomatunes.zomatunes.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.jetbrains.annotations.NotNull
import java.util.*


@Parcelize
data class Audiobook<ChapterType ,  AuthorType>(
   var narrator : String? = null,
   var chapters : List<@RawValue ChapterType>? = null,
   var intro : @RawValue ChapterType? = null,
   var book : String? = null,   // id of ebook format if available
   var lastListeningChapterIndex : Int = 0
) : Book<AuthorType>(),  Parcelable


@Parcelize
data class Rating(
   var _id: String? = null,
   var comment : String? = null,
   var userId : String? = null,
   var username : String? = null,
   var rate : Float,
   var bookId : String? = null,
   var date : Date? = null
) : Parcelable

data class BookHomeModel(
   var recent : List<Book<String>>? = null,
   var newAudiobooks : List<Book<String>>? = null,
   var newAndPopularAudiobooks : List<Book<String>>? = null,
   var bestSellingAudiobooks : List<Book<String>>? = null,
   var exclusiveAudiobooks : List<Book<String>>? = null,
   var commingSoonAudiobooks : List<Book<String>>? = null,
   var favoriteAuthorsBook : List<Book<String>>? = null,
   var favoriteCategoryBooks : List<Book<String>>? = null,

   var exclusiveBooks : List<Book<String>>? = null,
   var newBooks : List<Book<String>>? = null,
   var bestSellingBooks : List<Book<String>>? = null,

   var featuredBookCollection : List<BookCollection>? = null,
   var recommendedBookCollection : List<BookCollection>? = null,

   var categories : List<MusicBrowse>? = null,
   var bestsellerAuthors : List<Author<String , String>>? = null
)

data class BookSuggestion(
   var buyersRecommendationsAudiobooks : List<Book<String>>? = null,
   var buyersRecommendationsEbooks : List<Book<String>>? = null,
   var similarAudiobooks :  List<Book<String>>? = null,
   var similarEbooks :  List<Book<String>>? = null,
   var authorBooks :  List<Book<String>>? = null
)



@Entity
data class AudioBookDbInfo(
   @PrimaryKey
   @NotNull
   var _id : String,
   var name : String,
   var releaseDate : String,
   var coverImagePath : String,
   var authorName : String,
   var lastListeningChapterIndex : Int = 0
)