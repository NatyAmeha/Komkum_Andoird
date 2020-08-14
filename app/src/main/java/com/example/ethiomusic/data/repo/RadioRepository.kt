package com.example.ethiomusic.data.repo

import androidx.lifecycle.MutableLiveData
import com.example.ethiomusic.data.api.RadioApi
import com.example.ethiomusic.data.model.Radio
import com.example.ethiomusic.util.extensions.apiDataRequestHelper
import com.example.ethiomusic.util.extensions.dataRequestHelper
import javax.inject.Inject

class RadioRepository @Inject constructor(var radioApi: RadioApi){
    var error = MutableLiveData<String>()

     fun getFeaturedRadioStations() = apiDataRequestHelper<Any ,List<Radio> >(errorHandler = fun(message : String) = error.postValue(message)){
        radioApi.getFeaturedRadio()
    }

    fun getPopularRadioStations() = apiDataRequestHelper<Any ,List<Radio> >(errorHandler = fun(message : String) = error.postValue(message)){
        radioApi.getPopularRadio()
    }

    fun getRadioStation(radioId : String) = apiDataRequestHelper(radioId , fun(message : String) = error.postValue(message)){
        radioApi.getRadioStation(it!!)
    }

     fun getBrowseString() = apiDataRequestHelper<Any , List<String>>(errorHandler = fun(message : String) = error.postValue(message)){
        radioApi.getBrowseString()
    }

     fun getUserRadioStations() = apiDataRequestHelper<Any ,List<Radio> >(errorHandler = fun(message : String) = error.postValue(message)){
        radioApi.getUserRadio()
    }

    suspend fun getUserRadioStationSuspended() = dataRequestHelper<Any ,List<Radio> >(errorHandler = fun(message : String) = error.postValue(message)){
        radioApi.getUserRadio()
    }

    fun likeRadioStation(radioId : String) = apiDataRequestHelper(radioId , fun(message : String) = error.postValue(message)){
        radioApi.likeRadio(it!!)
    }
    fun unlikeRadioStation(radioId : String) = apiDataRequestHelper(radioId , fun(message : String) = error.postValue(message)){
        radioApi.unlikeRadio(it!!)
    }

    fun isRadioLIked(radioId : String) = apiDataRequestHelper(radioId , fun(message : String) = error.postValue(message)){
        radioApi.isradioLIked(it!!)
    }

    fun updateRadioStation(radio: Radio) = apiDataRequestHelper(radio , fun(message : String) = error.postValue(message)){
        radioApi.updateRadio(it!!)
    }

    suspend fun getRadioyGenre(genre : String) = dataRequestHelper(genre ,fun(message : String) = error.postValue(message)){
        radioApi.getRadioByGenre(it!!)
    }

    fun createradio(radio : Radio) = apiDataRequestHelper(radio ,fun(message : String) = error.postValue(message) ){
        radioApi.createRadio(it!!)
    }
}