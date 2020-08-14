package com.example.ethiomusic.data.repo

import android.R
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.ethiomusic.data.api.ArtistApi
import com.example.ethiomusic.data.model.Artist
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.extensions.apiDataRequestHelper
import com.example.ethiomusic.util.extensions.dataRequestHelper
import com.example.ethiomusic.util.extensions.toResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtistRepository @Inject constructor(var artistApi: ArtistApi) {

    var error = MutableLiveData<String>()

    fun getArtistInfo(artistId : String) = apiDataRequestHelper(artistId , fun(message) = error.postValue(message)){
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

    fun getArtistsbyGenre(genre : String) = apiDataRequestHelper(genre , fun(message : String) = error.postValue(message)){
        artistApi.getArtistByGenre(it!!)
    }



    fun followArtist(artistIds : List<String>) =  apiDataRequestHelper(artistIds , fun(message : String){error.postValue(message)}){
        artistApi.followArtist(it!!)
    }

    fun unfollowArtists(artistIds: List<String>) = apiDataRequestHelper(artistIds , fun(message : String) = error.postValue(message)){
        artistApi.unfollowArtist(it!!)
    }

    suspend fun isArtistInFavorite(artistId : String) :  Boolean {
        return try {
            withContext(Dispatchers.IO){
                var result = artistApi.checkArtistInFavorite(artistId).toResource()
                result.data!!
            }
        }catch (ex : Throwable){
            Log.i("apierror" , ex.message)
            error.value = ex.message
            false
        }
    }

    fun getArtistSearchResult(query : String) = apiDataRequestHelper(query , fun(message : String) = error.postValue(message) ){
        artistApi.getArtistSearch(it!!)
    }


}