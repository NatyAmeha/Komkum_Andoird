package com.komkum.komkum.data.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.komkum.komkum.data.api.LibraryApi
import com.komkum.komkum.data.model.Library
import com.komkum.komkum.data.model.LibraryInfo
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.toResource
import javax.inject.Inject

class LibraryRepository @Inject constructor (var libraryApi: LibraryApi) {

    var error = MutableLiveData<String>()

    fun getLibrarySummery() = apiDataRequestHelper<Any , Library>(errorHandler = fun(message : String){error.postValue(message)}){
        libraryApi.getLibrary()
    }

}