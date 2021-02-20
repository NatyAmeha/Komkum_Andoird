package com.zomatunes.zomatunes.ui.author

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zomatunes.zomatunes.data.model.AuthorMetadata
import com.zomatunes.zomatunes.data.repo.AuthorRepository
import kotlinx.coroutines.launch

class AuthorViewModel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle , var authorRepo : AuthorRepository) : ViewModel() {
    var error  = authorRepo.error
    var authorMetadataResult = MutableLiveData<AuthorMetadata>()

   fun getAuthorData(authorId : String) = viewModelScope.launch {
       val result = authorRepo.getAuthorInfo(authorId).data
       authorMetadataResult.value = result
   }

    fun followAuthor(authorId: String) = authorRepo.followAuthor(listOf(authorId))

    fun unfollowAuthor(authorId: String) = authorRepo.unfollowAuthor(listOf(authorId))

    suspend fun isAuthorInFavorite(authorId: String) = authorRepo.isAuthorInFavorite(authorId).data
}