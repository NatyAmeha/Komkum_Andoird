package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.AdApi
import com.komkum.komkum.util.extensions.dataRequestHelper
import javax.inject.Inject

class AdRepository @Inject constructor(var adApi : AdApi) {
    var error = MutableLiveData<String>()

    suspend fun updateAdsImpression(adId : String) = dataRequestHelper(adId , fun(message : String) = error.postValue(message)){
        adApi.updateAdImpression(it!!)
    }

    suspend fun updateAdClick(adId : String) = dataRequestHelper(adId , fun(message : String) = error.postValue(message)){
        adApi.updateAdClick(it!!)
    }

    suspend fun getGameAds(limit : Int) = dataRequestHelper(limit , fun(message : String) = error.postValue(message)){
        adApi.getGameAds(it!!)
    }

    suspend fun getTriviaQuestions(adId : String) = dataRequestHelper(adId  , fun(message : String) = error.postValue(message)){
        adApi.getQuestion(it!!)
    }


}