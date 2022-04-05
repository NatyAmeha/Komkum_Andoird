package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.komkum.komkum.data.api.HomeApi
import com.komkum.komkum.data.api.LibraryApi
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.*
import com.komkum.komkum.ui.download.DownloadViewmodel
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
import com.komkum.komkum.util.extensions.toResource
import com.komkum.komkum.util.extensions.toSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class PlaylistRepository @Inject constructor(var homeApi: HomeApi , var libraryApi: LibraryApi , var appDb: AppDb) {
    var error = MutableLiveData<String>()

    fun getRecommendationTags() = liveData {
        emit(Resource.Loading())
        try {
            var result = homeApi.getSongsByTags().toResource()
            delay(4000)
            emit(result)
        } catch (ex: Throwable) {
            error.value = ex.message
        }
    }

    suspend fun songRecommendationByArtist() = dataRequestHelper<Any , Playlist<Song<String, String>>>(errorHandler = fun(message : String) = error.postValue(message)){
        homeApi.getSongRecommendationByArtist()
    }

    suspend fun getPlaylist(playlistId : String , loadFromCache : Boolean = false) : Resource<Playlist<Song<String, String>>>{
       return if(loadFromCache){
            withContext(Dispatchers.IO){
                var playlistInfo = appDb.playlistDao.getPlaylist(playlistId)
                var songinfo = appDb.playlistSongJoinDao.getPlaylistSongs(playlistId)
                var playlist = Playlist(playlistInfo._id , name = playlistInfo.name , songs = songinfo.toSong())
                Resource.Success(data = playlist)
            }
        }else{
            dataRequestHelper(playlistId , fun(message : String) = error.postValue(message) ){
                libraryApi.getPlaylist(it!!)
            }
        }

    }

    suspend fun getUserPublicPlaylist(userId : String) = dataRequestHelper(userId ,  fun(message : String) = error.postValue(message)){
        libraryApi.getpublicPlaylist(it!!)
    }

    fun getFeaturedPlaylist(genre : String? = null) = apiDataRequestHelper(genre , fun(message : String) = error.postValue(message)){
        libraryApi.geFeaturedPlaylists()
    }

    fun getChartsList() = apiDataRequestHelper<Any , List<Playlist<String>>>(errorHandler = fun(message : String) = error.postValue(message)){
        libraryApi.getCharts()
    }

    suspend fun getNewSongPLaylists(genre : String? = null) = dataRequestHelper<Any , List<Playlist<String>>>(errorHandler = fun(message : String) = error.postValue(message)){
         libraryApi.getnewSongPlaylists(genre)
     }

    suspend fun getUserPlaylist() = dataRequestHelper<Any , List<Playlist<String>>>(errorHandler = fun(message : String) = error.postValue(message)){
        libraryApi.getUserPlaylists()
    }

    suspend fun makePlaylistPublic(playlistId : String) = dataRequestHelper(playlistId , fun(message : String) = error.postValue(message)){
        libraryApi.makePlaylistPublic(it!!)
    }

    fun createPlaylist(playlist : Playlist<String>) = apiDataRequestHelper(playlist , fun(message : String) = error.postValue(message)){
        libraryApi.createPlaylist(it!!)
    }

    fun updatePlaylist(playlist: Playlist<String>) = apiDataRequestHelper(playlist , fun(message : String) = error.postValue(message)){
        libraryApi.updatePlaylist(it!!)
    }

     suspend fun isPlaylistInFavorite(playlistId : String) = dataRequestHelper(playlistId , fun(message : String) = error.postValue(message)){
        libraryApi.checkPlaylistInLibrary(it!!)
    }

    fun addPlaylistTofavorite(playlistId : String) = apiDataRequestHelper(playlistId , fun(message : String) = error.postValue(message)){
        libraryApi.addPlaylistToFavorite(it!!)
    }

    fun removePlaylistFromFavorite(playlistId : String) = apiDataRequestHelper(playlistId , fun(message : String) = error.postValue(message)){
        libraryApi.removePlaylistFromFavorite(it!!)
    }

    fun addSongstoPlaylist(playlist : Playlist<String>) = apiDataRequestHelper(playlist ,fun(message : String) = error.postValue(message)){
        libraryApi.addSongToPlaylist(it!!)
    }

     fun removeSongFromPlaylist(playlist : Playlist<String>) = apiDataRequestHelper(playlist , fun(message : String) = error.postValue(message)){
        libraryApi.removeSongsFromPlaylist(it!!)
    }

    fun deletePlaylist(playlistId : String) = apiDataRequestHelper(playlistId , fun(message : String) = error.postValue(message)){
        libraryApi.deletePlaylist(it!!)
    }


}