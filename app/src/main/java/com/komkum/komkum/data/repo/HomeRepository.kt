package com.komkum.komkum.data.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.komkum.komkum.data.api.HomeApi
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.ProductHomepageViewmodel
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
import com.komkum.komkum.util.extensions.toResource
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

    fun getProductHomepage(long: Double? = null , lat : Double? = null) = apiDataRequestHelper<Any , ProductHomepageViewmodel>(errorHandler = fun(message : String){error.postValue(message)}){
        homeApi.getProductHomepage(long , lat)
    }

    suspend fun getProductHomepageBeta(long: Double? = null , lat : Double? = null) = dataRequestHelper<Any , ProductHomepageViewmodel>(errorHandler = fun(message : String){error.postValue(message)}){
        homeApi.getProductHomepage(long , lat)
    }


    fun getRecommendationTags() = apiDataRequestHelper<Any , List<Playlist<String>>>(errorHandler = fun(message : String){ error.postValue(message)}){
        homeApi.getSongsByTags()
    }

    suspend fun getSearchResult(query : String , loadFromCache : Boolean = false) = dataRequestHelper(query , fun(message : String) = error.postValue(message)){
       homeApi.getSearch(it!!)
    }

    suspend fun getSearchRecommendation() = dataRequestHelper<Any , Search>(errorHandler = fun(message : String) = error.postValue(message)){
        homeApi.getSearchRecommendation()
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

    suspend fun getDonations(creatorId : String) = dataRequestHelper(creatorId ,fun(message : String) = error.postValue(message)){
        homeApi.getDonations(it!!)
    }

    suspend fun getTopDonators(category : Int , size : Int) = dataRequestHelper(category ,fun(message : String) = error.postValue(message)){
        homeApi.getTopDonators(it!! , size)
    }

    suspend fun getTopCreatorsByDonation(category : Int , size : Int) = dataRequestHelper(category ,fun(message : String) = error.postValue(message)){
        homeApi.getMostDonatedCreators(it!! , size)
    }
}