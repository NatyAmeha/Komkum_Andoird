package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.SongApi
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.util.extensions.apiDataCheckHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
import javax.inject.Inject


class SongRepository @Inject constructor (var songApi : SongApi) {
    var error = MutableLiveData<String>()


    suspend fun getSongs(filter : String) = dataRequestHelper<Any , List<Song<String,String>>>(errorHandler = fun(message : String){error.postValue(message)}){
        songApi.getSongs(filter)
    }

    suspend fun getSongsBeta(filter : String) = dataRequestHelper<Any , List<Song<String,String>>>(errorHandler = fun(message : String){error.postValue(message)}){
        songApi.getSongs(filter)
    }

    suspend fun getSongsList(songsId : List<String>) = dataRequestHelper(songsId ,  fun(message : String){error.postValue(message)}){
        songApi.getSongsBeta(it!!)
    }

    suspend  fun getLikedSongs() = dataRequestHelper<Any , Playlist<Song<String,String>>>(errorHandler = fun(message :String){error.postValue(message)}){
        songApi.getLikedSongs("song")
    }

    suspend  fun getplaylistREcommendation() = dataRequestHelper<Any , List<Playlist<Song<String,String>>>>(errorHandler = fun(message :String){error.postValue(message)}){
        songApi.getplaylistRecommendations()
    }

    suspend fun isSongInFavorite(songId : String) = apiDataCheckHelper(songId , fun(message :String){error.postValue(message)}){
        songApi.checkSongInFavorite(it!!)
    }

    suspend fun likeSong(songIds : List<String>) = dataRequestHelper(songIds , fun(message :String){error.postValue(message)}){
        songApi.likeSong(it!!)
    }

   suspend fun unlikeSong(songIds: List<String>) = dataRequestHelper(songIds , fun(message :String){error.postValue(message)}){
        songApi.unlikeSong(it!!)
    }

   suspend fun updateSongStreamCount(songId : String) = dataRequestHelper(songId ,fun(message :String){error.postValue(message)}){
        songApi.updateStreamcount(it!!)
   }

    suspend fun downloadSong(songId: String) = dataRequestHelper(songId ,fun(message :String){error.postValue(message)}){
        songApi.downloadSong(it!!)
    }

    suspend fun getSongSearchREsult(query : String) = dataRequestHelper(query , fun(message :String) = error.postValue(message)){
        songApi.getSongSearchResult(it!!)
    }

    suspend fun getBrowseCategories(browseCategory : String) = dataRequestHelper<Any , List<MusicBrowse>>(errorHandler =  fun(message :String) = error.postValue(message)){
        songApi.getBrowseItems(browseCategory)
    }


}