package com.zomatunes.zomatunes.data.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.zomatunes.zomatunes.data.api.LibraryApi
import com.zomatunes.zomatunes.data.model.Library
import com.zomatunes.zomatunes.data.model.LibraryInfo
import com.zomatunes.zomatunes.util.Resource
import com.zomatunes.zomatunes.util.extensions.apiDataRequestHelper
import com.zomatunes.zomatunes.util.extensions.toResource
import javax.inject.Inject

class LibraryRepository @Inject constructor (var libraryApi: LibraryApi) {

    var error = MutableLiveData<String>()

    fun getLibrarySummery() = apiDataRequestHelper<Any , Library>(errorHandler = fun(message : String){error.postValue(message)}){
        libraryApi.getLibrary()
    }

}