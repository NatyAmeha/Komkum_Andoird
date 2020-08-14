package com.example.ethiomusic.data.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.example.ethiomusic.data.api.LibraryApi
import com.example.ethiomusic.data.model.Library
import com.example.ethiomusic.data.model.LibraryInfo
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.extensions.apiDataRequestHelper
import com.example.ethiomusic.util.extensions.toResource
import javax.inject.Inject

class LibraryRepository @Inject constructor (var libraryApi: LibraryApi) {

    var error = MutableLiveData<String>()

    fun getLibrarySummery() = apiDataRequestHelper<Any , Library>(errorHandler = fun(message : String){error.postValue(message)}){
        libraryApi.getLibrary()
    }

}