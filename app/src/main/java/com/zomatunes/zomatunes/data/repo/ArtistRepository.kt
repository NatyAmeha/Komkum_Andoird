package com.zomatunes.zomatunes.data.repo

import android.R
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.zomatunes.zomatunes.data.api.ArtistApi
import com.zomatunes.zomatunes.data.model.Artist
import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.util.Resource
import com.zomatunes.zomatunes.util.extensions.apiDataRequestHelper
import com.zomatunes.zomatunes.util.extensions.dataRequestHelper
import com.zomatunes.zomatunes.util.extensions.toResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtistRepository @Inject constructor(var artistApi: ArtistApi) {

    var error = MutableLiveData<String>()

    suspend fun getArtistInfo(artistId : String) = dataRequestHelper(artistId , fun(message) = error.postValue(message)){
        artistApi.getArtist(it!!)
    }


    fun getPopularArtists() = apiDataRequestHelper<Any , List<Artist<String,String>>>(errorHandler =  fun(message) = error.postValue(message)){
        artistApi.getPopularArtist()
    }
    suspend fun getPopularArtistsSuspend() = dataRequestHelper<Any , List<Artist<String,String>>>(errorHandler =  fun(message) = error.postValue(message)){
        artistApi.getPopularArtist()
    }


    fun getNewArtists() = liveData {
        try {
            emit(Resource.Loading())
            withContext(Dispatchers.IO){
                var result = artistApi.getnewArtists().toResource()
                emit(result)
            }
        }catch (ex : Throwable){
            Log.i("apierror" , ex.message)
            error.value = ex.message
        }
    }

    fun getUserFavoriteArtists() = liveData {
        try {
            emit(Resource.Loading())
            withContext(Dispatchers.IO){
                var result = artistApi.getFavoriteArtist().toResource()
                emit(result)
            }
        }catch (ex : Throwable){
            Log.i("apierror" , ex.message)
            error.value = ex.message
        }
    }

    suspend fun getUserFavoriteARtistsBeta() = dataRequestHelper<Any , List<Artist<String , String>>>(errorHandler = fun(message : String) = error.postValue(message)){
        artistApi.getFavoriteArtist()
    }

    suspend fun getFavoriteArtist(userId : String) = dataRequestHelper(userId , fun(message : String) = error.postValue(message)){
        artistApi.getFavoriteArtist(it!!)
    }

    fun getArtistsbyGenre(genre : String) = apiDataRequestHelper(genre , fun(message : String) = error.postValue(message)){
        artistApi.getArtistByGenre(it!!)
    }



    fun followArtist(artistIds : List<String>) =  apiDataRequestHelper(artistIds , fun(message : String){error.postValue(message)}){
        artistApi.followArtist(it!!)
    }

    fun unfollowArtists(artistIds: List<String>) = apiDataRequestHelper(artistIds , fun(message : String) = error.postValue(message)){
        artistApi.unfollowArtist(it!!)
    }

    suspend fun isArtistInFavorite(artistId : String) = dataRequestHelper(artistId , fun(message : String) = error.postValue(message) ) {
        artistApi.checkArtistInFavorite(it!!)
    }

    fun getArtistSearchResult(query : String) = apiDataRequestHelper(query , fun(message : String) = error.postValue(message) ){
        artistApi.getArtistSearch(it!!)
    }


}