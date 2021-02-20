package com.zomatunes.zomatunes.data.repo

import androidx.lifecycle.MutableLiveData
import com.zomatunes.zomatunes.data.api.RadioApi
import com.zomatunes.zomatunes.data.model.Radio
import com.zomatunes.zomatunes.data.model.UserRadioInfo
import com.zomatunes.zomatunes.util.extensions.apiDataRequestHelper
import com.zomatunes.zomatunes.util.extensions.dataRequestHelper
import javax.inject.Inject

class RadioRepository @Inject constructor(var radioApi: RadioApi){
    var error = MutableLiveData<String>()

    fun getFeaturedRadioStations() = apiDataRequestHelper<Any ,List<Radio> >(errorHandler = fun(message : String) = error.postValue(message)){
        radioApi.getFeaturedRadio()
    }

    suspend fun getRadioStationByCategory(category : String) = dataRequestHelper(category , fun(message : String) = error.postValue(message)){
        radioApi.getRadiobyCategory(it!!)
    }

    suspend fun getSongRadioStation(songId : String) = dataRequestHelper(songId , fun(message : String) = error.postValue(message)){
        radioApi.getRadioFromSong(it!!)
    }

    suspend fun getArtistRadioStation(artistId  : String) = dataRequestHelper(artistId , fun(message : String) = error.postValue(message)){
        radioApi.getRadioFromArtist(it!!)
    }


    fun getPopularRadioStations() = apiDataRequestHelper<Any ,List<Radio> >(errorHandler = fun(message : String) = error.postValue(message)){
        radioApi.getPopularRadio()
    }

    suspend fun getRadioStation(radioId : String) = dataRequestHelper(radioId , fun(message : String) = error.postValue(message)){
        radioApi.getRadioStation(it!!)
    }

     fun getBrowseString() = apiDataRequestHelper<Any , List<String>>(errorHandler = fun(message : String) = error.postValue(message)){
        radioApi.getBrowseString()
    }

     suspend fun getUserRadioStations() = dataRequestHelper<Any ,List<Radio> >(errorHandler = fun(message : String) = error.postValue(message)){
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

    fun updateRadioStation(userRadioInfo: UserRadioInfo) = apiDataRequestHelper(userRadioInfo , fun(message : String) = error.postValue(message)){
        radioApi.updateRadio(it!!)
    }

    suspend fun getRadioyGenre(genre : String) = dataRequestHelper(genre ,fun(message : String) = error.postValue(message)){
        radioApi.getRadioByGenre(it!!)
    }

    fun createradio(radio : Radio) = apiDataRequestHelper(radio ,fun(message : String) = error.postValue(message) ){
        radioApi.createRadio(it!!)
    }
}