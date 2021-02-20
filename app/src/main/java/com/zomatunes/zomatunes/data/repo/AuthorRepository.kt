package com.zomatunes.zomatunes.data.repo

import androidx.lifecycle.MutableLiveData
import com.zomatunes.zomatunes.data.api.AuthorApi
import com.zomatunes.zomatunes.util.extensions.apiDataRequestHelper
import com.zomatunes.zomatunes.util.extensions.dataRequestHelper
import javax.inject.Inject

class AuthorRepository @Inject constructor(var authorApi: AuthorApi) {
    var error = MutableLiveData<String>()

    suspend fun getAuthorInfo(authorId : String) = dataRequestHelper(authorId , fun(message) = error.postValue(message)){
        authorApi.getAuthorInfo(it!!)
    }

    fun followAuthor(authorsId : List<String>) = apiDataRequestHelper(authorsId ,  fun(message) = error.postValue(message)){
        authorApi.followAuthor(it!!)
    }

    fun unfollowAuthor(authorsId: List<String>) = apiDataRequestHelper(authorsId ,  fun(message) = error.postValue(message)){
        authorApi.unfollowAuthor(it!!)
    }

    suspend fun isAuthorInFavorite(authorId : String) = dataRequestHelper(authorId ,  fun(message) = error.postValue(message)){
        authorApi.isAuthorInFavorite(it!!)
    }
}