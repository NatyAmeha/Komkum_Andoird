package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Keep
data class Search(
    var songs : List<Song<String,String>>? = null,
    var albums : List<Album<String,String>>? = null,
    var artists : List<Artist<String,String>>? = null,
    var playlists : List<Playlist<String>>?=null,
    var audiobooks : List<Book<String>>? = null,
    var books : List<Book<String>>? = null,
    var authors : List<Author<String , String>>? = null,

    var episodes : List<PodcastEpisode>? = null,
    var podcasts : List<Podcast<String>>? = null,
    var products : List<Product>? = null,
    var packages : List<ProductPackage<Product>>? = null
) : Parcelable

@Entity
data class SearchSuggestion(
    @PrimaryKey(autoGenerate = true)
    var _id : Int,
    var name : String,
    var date : String
)