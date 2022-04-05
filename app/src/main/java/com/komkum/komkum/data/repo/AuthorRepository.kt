package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.AuthorApi
import com.komkum.komkum.data.model.Author
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
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

    suspend fun getUserFvoriteAuthors() = dataRequestHelper<Any , List<Author<String,String>>>(errorHandler = fun(message) = error.postValue(message)){
        authorApi.getUserFavorites()
    }
}