package com.zomatunes.zomatunes.ui.radio

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.IViewmodelFactory
import com.zomatunes.zomatunes.data.model.Radio
import com.zomatunes.zomatunes.data.repo.RadioRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class RadioViewModel @ViewModelInject constructor(@Assisted var savedState : SavedStateHandle, var radioRepo : RadioRepository) : ViewModel() {
    var featuredRadioStations = radioRepo.getFeaturedRadioStations()
    var radioStationList = MutableLiveData<List<Radio>>()

    var radioStation = MutableLiveData<Radio>()

    fun getRadioStationResult(radioId : String) = viewModelScope.launch {
        radioStation.value = radioRepo.getRadioStation(radioId).data
    }

    fun getSongRadio(songId : String) = viewModelScope.launch {
        radioStation.value = radioRepo.getSongRadioStation(songId).data
    }
    fun getArtistRadio(artistId : String) = viewModelScope.launch {
        radioStation.value = radioRepo.getArtistRadioStation(artistId).data
    }

    fun getRadiosByCategory(category : String) = viewModelScope.launch {
        var result = radioRepo.getRadioStationByCategory(category)
        radioStationList.value = result.data
    }
    fun  userRadioStations() = viewModelScope.launch {
        var result = radioRepo.getUserRadioStations()
        radioStationList.value = result.data
    }

    fun createRadioStation(radio : Radio) = radioRepo.createradio(radio)
    fun likeRadioStation(radioId : String) = radioRepo.likeRadioStation(radioId)
    fun unlikeRadioStation(radioId : String) = radioRepo.unlikeRadioStation(radioId)
    fun isRadioStationLiked(radioId : String) = radioRepo.isRadioLIked(radioId)

}

class RadioViewmodelFactory @Inject constructor(var radioRepo : RadioRepository) : IViewmodelFactory<RadioViewModel>{
    override fun create(savedStateHandle: SavedStateHandle): RadioViewModel {
        return RadioViewModel(savedStateHandle , radioRepo)
    }

}