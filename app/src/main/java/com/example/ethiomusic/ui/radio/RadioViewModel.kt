package com.example.ethiomusic.ui.radio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.model.Radio
import com.example.ethiomusic.data.repo.RadioRepository
import javax.inject.Inject

class RadioViewModel(var savedState : SavedStateHandle , var radioRepo : RadioRepository) : ViewModel() {
    var featuredRadioStations = radioRepo.getFeaturedRadioStations()
    fun getRadioStationResult(radioId : String) = radioRepo.getRadioStation(radioId)
    var userRadioStations = radioRepo.getUserRadioStations()

    fun createRadioStation(radio : Radio) = radioRepo.createradio(radio)
    fun likeRadioStation(radioId : String) = radioRepo.likeRadioStation(radioId)
    fun unlikeRadioStation(radioId : String) = radioRepo.unlikeRadioStation(radioId)
    fun isRadioStationLiked(radioId : String) = radioRepo.isRadioLIked(radioId)

    fun updateRadioStation(radio: Radio) = radioRepo.updateRadioStation(radio)
}

class RadioViewmodelFactory @Inject constructor(var radioRepo : RadioRepository) : IViewmodelFactory<RadioViewModel>{
    override fun create(savedStateHandle: SavedStateHandle): RadioViewModel {
        return RadioViewModel(savedStateHandle , radioRepo)
    }

}