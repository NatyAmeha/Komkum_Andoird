package com.komkum.komkum.ui.radio

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.IViewmodelFactory
import com.komkum.komkum.data.model.Radio
import com.komkum.komkum.data.model.RadioLikeInfo
import com.komkum.komkum.data.repo.RadioRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class RadioViewModel @ViewModelInject constructor(@Assisted var savedState : SavedStateHandle, var radioRepo : RadioRepository) : ViewModel() {
    var featuredRadioStations = radioRepo.getFeaturedRadioStations()

    var error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(radioRepo.error){ error.value = it }
        return a
    }
    fun removeOldErrors(){
        radioRepo.error.value = null
    }

    var radioStationList = MutableLiveData<List<Radio>>()

    var radioStation = MutableLiveData<Radio>()

    suspend fun getRadioStationResult(radioId : String) : Radio? {
//        radioStation.value = radioRepo.getRadioStation(radioId).data
        return radioRepo.getRadioStation(radioId).data
    }

    suspend fun getSongRadio(songId : String) : Radio? {
//        radioStation.value = radioRepo.getSongRadioStation(songId).data
        return radioRepo.getSongRadioStation(songId).data
    }
    suspend fun getArtistRadio(artistId : String) : Radio? {
//        radioStation.value = radioRepo.getArtistRadioStation(artistId).data
        return radioRepo.getArtistRadioStation(artistId).data
    }

    fun getRadiosByCategory(category : String) = viewModelScope.launch {
        var result = radioRepo.getRadioStationByCategory(category)
        radioStationList.value = result.data ?: null
    }
    fun  userRadioStations() = viewModelScope.launch {
        var result = radioRepo.getUserRadioStations()
        radioStationList.value = result.data ?: null
    }

    fun createRadioStation(radio : Radio) = radioRepo.createradio(radio)
    fun likeRadioStation(radioLikeInfo : RadioLikeInfo) = radioRepo.likeRadioStation(radioLikeInfo)
    fun unlikeRadioStation(radioId : String) = radioRepo.unlikeRadioStation(radioId)
    suspend fun isRadioStationLiked(radioId : String) = radioRepo.isRadioLIked(radioId).data

}

class RadioViewmodelFactory @Inject constructor(var radioRepo : RadioRepository) : IViewmodelFactory<RadioViewModel>{
    override fun create(savedStateHandle: SavedStateHandle): RadioViewModel {
        return RadioViewModel(savedStateHandle , radioRepo)
    }

}