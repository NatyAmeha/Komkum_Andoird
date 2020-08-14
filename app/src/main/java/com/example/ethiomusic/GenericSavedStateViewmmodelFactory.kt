package com.example.ethiomusic

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

interface IViewmodelFactory<T : ViewModel>{
    fun create(savedStateHandle: SavedStateHandle) : T
}

class GenericSavedStateViewmmodelFactory< V : ViewModel>(var viewmodelFactory: IViewmodelFactory<V> , var owner : SavedStateRegistryOwner , bundle: Bundle? = null) :
    AbstractSavedStateViewModelFactory(owner , bundle){
    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return viewmodelFactory.create(handle) as T
    }

}