package com.komkum.komkum.ui.author

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komkum.komkum.data.model.Author
import com.komkum.komkum.data.model.AuthorMetadata
import com.komkum.komkum.data.repo.AuthorRepository
import kotlinx.coroutines.launch

class AuthorViewModel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle , var authorRepo : AuthorRepository) : ViewModel() {
    var error  = authorRepo.error
    var authorMetadataResult = MutableLiveData<AuthorMetadata>()
    var authorsResult = MutableLiveData<List<Author<String,String>>>()

    fun getUserFavoriteAuthors() = viewModelScope.launch {
        authorsResult.value = authorRepo.getUserFvoriteAuthors().data ?: null
    }

   fun getAuthorData(authorId : String) = viewModelScope.launch {
       val result = authorRepo.getAuthorInfo(authorId).data
       authorMetadataResult.value = result ?: null
   }

    fun followAuthor(authorId: String) = authorRepo.followAuthor(listOf(authorId))

    fun unfollowAuthor(authorId: String) = authorRepo.unfollowAuthor(listOf(authorId))

    suspend fun isAuthorInFavorite(authorId: String) = authorRepo.isAuthorInFavorite(authorId).data
}