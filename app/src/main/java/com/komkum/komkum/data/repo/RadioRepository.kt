package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.RadioApi
import com.komkum.komkum.data.model.Radio
import com.komkum.komkum.data.model.RadioLikeInfo
import com.komkum.komkum.data.model.UserRadioInfo
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
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

    fun likeRadioStation(radioLikeInfo: RadioLikeInfo) = apiDataRequestHelper(radioLikeInfo , fun(message : String) = error.postValue(message)){
        radioApi.likeRadio(it!!)
    }
    fun unlikeRadioStation(radioId : String) = apiDataRequestHelper(radioId , fun(message : String) = error.postValue(message)){
        radioApi.unlikeRadio(it!!)
    }

    suspend fun isRadioLIked(radioId : String) = dataRequestHelper(radioId , fun(message : String) = error.postValue(message)){
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