package com.komkum.komkum.ui.library

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.IViewmodelFactory
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.AlbumRepository
import com.komkum.komkum.data.repo.LibraryRepository
import com.komkum.komkum.data.repo.PlaylistRepository
import com.komkum.komkum.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class LibraryViewModel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle, var libraryRepo: LibraryRepository,
                                                        var albumRepo: AlbumRepository, var playlistRepo : PlaylistRepository) : ViewModel() {

    var playlistData  = MutableLiveData<List<Playlist<String>>>()
    val error = libraryRepo.error

    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(albumRepo.error){ error.value = it }
        a.addSource(libraryRepo.error){ error.value = it}
        a.addSource(playlistRepo.error){error.value = it}
        return a
    }

    fun getUserPlaylist() = viewModelScope.launch {
       playlistData.value =  playlistRepo.getUserPlaylist().data ?: null
    }



    var librarySummery = libraryRepo.getLibrarySummery()


    fun createPlaylist(playlist : Playlist<String>) = playlistRepo.createPlaylist(playlist)


//    var favoriteAlbums = libraryRepo.libraryResponse.switchMap { library ->
//        liveData {
//            library?.let {
//                var result = it.likedAlbums?.let {
//                    it.map { libraryInfo -> albumRepo.getAlbum(libraryInfo.vId!!).value!!.data }
//                }
//                emit(Resource.Success(result))
//            }
//            emit(Resource.Loading<Any>())
//        }
//    }
}


class LibraryViewmodelFacotry @Inject constructor(var libraryRepo: LibraryRepository, var albumRepo: AlbumRepository, var playlistRepo : PlaylistRepository)
    : IViewmodelFactory<LibraryViewModel> {
    override fun create(savedStateHandle: SavedStateHandle): LibraryViewModel {
        return LibraryViewModel(savedStateHandle, libraryRepo, albumRepo , playlistRepo)
    }
}
