package com.example.ethiomusic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

data class Search(
    var songs : List<Song<String,String>>? = null,
    var albums : List<Album<String,String>>? = null,
    var artists : List<Artist<String,String>>? = null,
    var playlist : List<Playlist<String>>?=null
)

@Entity
data class SearchSuggestion(
    @PrimaryKey(autoGenerate = true)
    var _id : Int,
    var name : String,
    var date : String
)