package com.example.ethiomusic.data.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.ethiomusic.data.api.HomeApi
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.extensions.apiDataRequestHelper
import com.example.ethiomusic.util.extensions.dataRequestHelper
import com.example.ethiomusic.util.extensions.toResource
import kotlinx.coroutines.delay
import javax.inject.Inject

class HomeRepository @Inject constructor(var homeApi: HomeApi) {
    var error = MutableLiveData<String>()

    fun getHomeData(filter : String? = null) = apiDataRequestHelper<Any , Home>(errorHandler = fun(message : String){error.postValue(message)}){
        homeApi.getHome(filter)
    }

    fun getHomeBeta() = apiDataRequestHelper<Any , HomeBeta>(errorHandler = fun(message : String){error.postValue(message)}){
        homeApi.getHomeBeta()
    }


    fun getRecommendationTags() = apiDataRequestHelper<Any , List<Playlist<String>>>(errorHandler = fun(message : String){ error.postValue(message)}){
        homeApi.getSongsByTags()
    }

    suspend fun getSearchResult(query : String , loadFromCache : Boolean = false) = dataRequestHelper(query , fun(message : String) = error.postValue(message)){
       homeApi.getSearch(it!!)
    }

    suspend  fun getBrowseResult(browseInfo : MusicBrowse) = dataRequestHelper(browseInfo ,fun(message : String) = error.postValue(message)){
        homeApi.browseResult(it!!)
    }


    fun songRecommendationByArtist() = liveData {
//        emit(Resource.Loading<List<Playlist<Song>>>())
        try {
            var result = homeApi.getSongRecommendationByArtist().toResource()
            var data = listOf(result.data!!)
            var newresult = Resource.Success(data)
            emit(newresult)
        } catch (ex: Throwable) {
            error.value = ex.message
        }
    }
}