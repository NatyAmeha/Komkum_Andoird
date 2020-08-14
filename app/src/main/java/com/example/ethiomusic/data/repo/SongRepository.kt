package com.example.ethiomusic.data.repo

import androidx.lifecycle.MutableLiveData
import com.example.ethiomusic.data.api.SongApi
import com.example.ethiomusic.data.model.MusicBrowse
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.util.extensions.apiDataCheckHelper
import com.example.ethiomusic.util.extensions.apiDataRequestHelper
import com.example.ethiomusic.util.extensions.dataRequestHelper
import javax.inject.Inject


class SongRepository @Inject constructor (var songApi : SongApi) {
    var error = MutableLiveData<String>()


    suspend fun getSongs(filter : String) = dataRequestHelper<Any , List<Song<String,String>>>(errorHandler = fun(message : String){error.postValue(message)}){
        songApi.getSongs(filter)
    }

    suspend fun getSongsBeta(filter : String) = dataRequestHelper<Any , List<Song<String,String>>>(errorHandler = fun(message : String){error.postValue(message)}){
        songApi.getSongs(filter)
    }

    fun getSongsList(songsId : List<String>) = apiDataRequestHelper(songsId ,  fun(message : String){error.postValue(message)}){
        songApi.getSongsBeta(it!!)
    }

    suspend  fun getLikedSongs() = dataRequestHelper<Any , List<Song<String,String>>>(errorHandler = fun(message :String){error.postValue(message)}){
        songApi.getLikedSongs("song")
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

    fun getMusicBrowsingString(browseCategory : String) = apiDataRequestHelper<Any , List<MusicBrowse>>(errorHandler =  fun(message :String) = error.postValue(message)){
        songApi.getMusicBrowsingString(browseCategory)
    }
}